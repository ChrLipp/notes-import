import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.zip.GZIPInputStream

class Entry
{
	String key
	Long date
	String folder
	String title
	byte[] data
}

def log = LoggerFactory.getLogger('notes')
def entries = readEntries()
writeEntries(entries)

Entry[] readEntries()
{
	def url = 'jdbc:sqlite:input/NoteStore.sqlite'
	def conn = DriverManager.getConnection(url,'','')
	def stmt = conn.createStatement()

	ResultSet rs
	def result = []

	rs = stmt.executeQuery(
		'SELECT Z.Z_PK as key, _FOLDER.ZTITLE2 as folder, NOTEDATA.ZDATA as data, Z.ZCREATIONDATE1 as date, Z.ZTITLE1 as title FROM ZICCLOUDSYNCINGOBJECT as Z INNER JOIN ZICCLOUDSYNCINGOBJECT AS _FOLDER ON Z.ZFOLDER = _FOLDER.Z_PK INNER JOIN ZICNOTEDATA as NOTEDATA ON Z.ZNOTEDATA = NOTEDATA.Z_PK WHERE Z.Z_ENT = 8;'
	)

	while (rs.next()) {
		def key = rs.getString('key')
		// Core Date dates are later than UNIX dates by 978307200 seconds
		def date = (rs.getLong('date') + 978307200) * 1000
		def folder = rs.getString('folder')
		def title = rs.getString('title')
		def data = rs.getBytes('data')
		result << new Entry(key: key, data: data, date: date, folder: folder, title: title)
	}
	conn.close()
	result
}

void writeEntries(Entry[] entries)
{
	entries.each { entry ->
		if(entry.data) {
			def directoryPath = "output/${entry.folder}"
			def directory = new File(directoryPath);
			if (!directory.exists()){
				directory.mkdir();
			}

			// Remove forward slashes from note titles
			def filename = "${entry.key} - ${entry.title.replaceAll("/", "")}";
			def filenameWithPath = "output/${entry.folder}/${filename}.txt"

			def output = new File(filenameWithPath)
			output.text = getPlainText(decompress(entry.data)).trim()
			output.setLastModified(entry.date)
		}
	}
}

String decompress(byte[] input)
{
	def gzipInput = new GZIPInputStream(new ByteArrayInputStream(input))
	def stringWriter = new StringWriter()
	IOUtils.copy(gzipInput, stringWriter, StandardCharsets.UTF_8)
	stringWriter.toString()
}

String getPlainText(String input)
{
	// Find the apparent magic string 08 00 10 00 1a
	def startIndex = input.indexOf('\u0008\u0000\u0010\u0000\u001a')

	// Find the next byte after 0x12
	startIndex = input.indexOf('\u0012', startIndex + 1) + 2

	// Read the end index
	def endIndex = input.indexOf('\u0004\u0008\u0000\u0010\u0000\u0010\u0000\u001a\u0004\u0008\u0000', startIndex)

	input.substring(startIndex, endIndex)
}

String asciiCode(String input)
{
	def result = []
	input.each { character ->
		result << asciiCodeForChar(character)
	}
	result.join(', ')
}

String asciiCodeForChar(String input)
{
	input.format("%04x", new BigInteger(1, input.getBytes(StandardCharsets.UTF_8)))
}
