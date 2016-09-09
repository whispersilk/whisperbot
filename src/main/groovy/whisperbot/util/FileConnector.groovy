package whisperbot.util

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import java.util.regex.Pattern

class FileConnector {

    private File file;
    private static final String PATH_TO_RESOURCES = "${System.getProperty('user.dir')}/src/data/"

    FileConnector(String name) {
        String fileName = "${PATH_TO_RESOURCES}${name}".replace('\\' as char, File.separator as char).replace('/' as char, File.separator as char)
        file = new File(fileName)
        File parent = file.getParentFile()
        if(!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create directory: $parent")
        }
        file.createNewFile()
    }

    FileConnector(String dir, String name) {
        file = new File(dir, name)
        file.createNewFile()
    }

    FileConnector(File fileObj) {
        file = fileObj
        file.createNewFile()
    }

    boolean save(def o) {
        if(!(o instanceof Map) && !(o instanceof List)) {
            return false
        }
        file.delete()
        file.createNewFile()
        file.withWriter { writer ->
            writer.write JsonOutput.prettyPrint(JsonOutput.toJson(o))
        }
        return true
    }

    def load(String nullVal = 'null') {
        String jsonString = file.collect { it }.join(' ')
        if(!jsonString) {
            if (nullVal == 'map') { return [:] }
            else if (nullVal == 'list') { return [] }
            else { return null }
        }
        return new JsonSlurper().parseText(jsonString)
    }
}