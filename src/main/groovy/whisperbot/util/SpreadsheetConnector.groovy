package whisperbot.util

import java.net.URL

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.ListEntry
import com.google.gdata.data.spreadsheet.ListFeed
import com.google.gdata.data.spreadsheet.SpreadsheetEntry
import com.google.gdata.data.spreadsheet.SpreadsheetFeed
import com.google.gdata.data.spreadsheet.WorksheetEntry
import com.google.gdata.util.AuthenticationException

import whisperbot.exception.SpreadsheetConnectionException

class SpreadsheetConnector {

    private static GoogleCredential credential

    // A reference to the file storing 
    private static URL CREDENTIAL_KEY
    
    // List of authorized scopes. The first element should never change - it's used elsewhere.
    private static final List SCOPES = ['https://spreadsheets.google.com/feeds/spreadsheets/private/full', 'https://spreadsheets.google.com/feeds', 'https://docs.google.com/feeds']

    private String spreadsheetName
    private String worksheetName
    private SpreadsheetService service
    private WorksheetEntry worksheet
    private ListFeed worksheetFeed

    SpreadsheetConnector(String spreadsheetName, String worksheetName) {
        this.spreadsheetName = spreadsheetName
        this.worksheetName = worksheetName
    }

    SpreadsheetConnector init(String oauthJson) {
        if(!CREDENTIAL_KEY) {
            CREDENTIAL_KEY = this.getClass().getResource(oauthJson)
        }
        ensureAuthorized()
        service = new SpreadsheetService('Reshy_spreadsheets')
        try {
            service.setOAuth2Credentials(credential)
        }
        catch (AuthenticationException e) {
            e.printStackTrace()
        }
        SpreadsheetFeed feed = service.getFeed(new URL(SCOPES[0]), SpreadsheetFeed)
        SpreadsheetEntry spreadsheet = feed.getEntries().find { it.getTitle().getPlainText() == spreadsheetName }
        if(!spreadsheet) {
            throw new SpreadsheetConnectionException('No spreadsheet exists with that name.')
        }
        worksheet = spreadsheet.getWorksheets().find { it.getTitle().getPlainText() == worksheetName }
        if(!worksheet) {
            throw new SpreadsheetConnectionException('No worksheet exists with that name.')
        }
        worksheetFeed = service.getFeed(worksheet.getListFeedUrl(), ListFeed)
        if(!worksheetFeed) {
            throw new SpreadsheetConnectionException('Coundn\'t get feed for worksheet.')
        }
        return this
    }

    private void ensureAuthorized() {
        if(!credential) {
            credential = GoogleCredential.fromStream(CREDENTIAL_KEY.openStream()).createScoped(SCOPES)
        }
        else if(credential.getExpiresInSeconds() < 10) {
            credential.refreshToken()
        }
    }

    Set columns() {
        ensureAuthorized()
        return worksheetFeed.getEntries()[0].getCustomElements().getTags()
    }

    ListEntry find(String column, String query, String mode = 'equals') {
        ensureAuthorized()
        if(mode == 'equals') {
            return worksheetFeed.getEntries().find { it.getCustomElements().getValue(column) == query }
        }
    }

    List findAll(String column, String query, String mode = 'equals') {
        ensureAuthorized()
        if(mode == 'equals') {
            return worksheetFeed.getEntries().findAll { it.getCustomElements().getValue(column) == query }
        }
        else if(mode == 'equals_insensitive') {
            return worksheetFeed.getEntries().findAll { it.getCustomElements().getValue(column) instanceof String && it.getCustomElements().getValue(column).equalsIgnoreCase(query) }
        }
        else if(mode == 'contains_as_word') {
            return worksheetFeed.getEntries().findAll { it.getCustomElements().getValue(column) instanceof String && it.getCustomElements().getValue(column).split(' ').find { word -> word.equalsIgnoreCase(query) } }
        }
        else if(mode == 'contains') {
            return worksheetFeed.getEntries().findAll { it.getCustomElements().getValue(column) instanceof String && it.getCustomElements().getValue(column).find(/(?i)${query}(?-i)/) }
        }
        else {
            return []
        }
    }

    List getColumnByName(String column) {
        ensureAuthorized()
        return worksheetFeed.getEntries().collect { it.getCustomElements().getValue(column) }
    }

    Map parseField(List fields, ListEntry entry) {
        Map entryMap = [:]
        fields.each { field ->
            entryMap.put(field, entry.getCustomElements().getValue(field))
        }
        return entryMap
    }

    List parseFields(List fields, List entries) {
        List toReturn = []
        entries.each { entry ->
            toReturn << parseField(fields, entry)
        }
        return toReturn
    }
}