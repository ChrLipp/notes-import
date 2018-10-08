# Apple Notes database parser

## About
Apple Notes data is stored in a SQLite database. Some month ago the data has been stored in the file
`/Users/<user>/Library/Containers/com.apple.Notes/Data/Library/Notes/NotesV7.storedata` respectively
`NotesV6.storedata` for the prior version. The notes have been stored in a HTML subset in clear text.

Then Apple kept the file but didn't update it anymore. Instead, a new location was used: 
`/Users/christian/Library/Group Containers/group.com.apple.notes/NoteStore.sqlite`. The data is not
in clear text anymore. I needed the access badly, because my MacBook Pro was defect (spilled a bottle 
of wine over it), my last backup was long ago and I didn't sync into iCloud.  

So I found [apple_cloud_notes_parser](https://github.com/threeplanetssoftware/apple_cloud_notes_parser),
a Perl script which showed me how to parse the data. However, they have a small bug in calculation the
length of the data, so you will get only the first 27 chars or so of the note.

I decided to port the code to Java (ok, to Groovy) to be able to fix that. I could have adopted the
perl script but I thought I have to strip also the HTML fragments, but this was an error. In the new
format, there are no HTML fragments any more.

## Usage

Copy the database (location above) to the input folder and receive a file per document in the output
folder. Afterwards I created empty notes and copy/pasted the notes back to Apple Notes again (at this time
with activated iCloud sync).

## Remark

I also tried to copy the whole database to my new MacBook, but this didn't work.
