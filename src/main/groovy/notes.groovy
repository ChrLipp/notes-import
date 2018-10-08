import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.zip.GZIPInputStream

class Entry
{
	String key
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

	rs = stmt.executeQuery('select Z_PK, ZDATA from ZICNOTEDATA')
	while (rs.next()) {
		def key = rs.getString('Z_PK')
		def data = rs.getBytes('ZDATA')
		result << new Entry(key: key, data: data)
	}
	conn.close()
	result
}

void writeEntries(Entry[] entries)
{
	entries.each { entry ->
		def output = new File("output/${entry.key}.txt")
		output.text = getPlainText(decompress(entry.data)).trim()
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