# Notes

Miscellaneous project-related notes that don't belong anywhere else.

## Markdown to BBCode

To convert a PMS release [changelog](https://github.com/ps3mediaserver/ps3mediaserver/blob/master/CHANGELOG.txt)
to [BBCode](http://www.bbcode.org/) format (e.g. for a release
[announcement](http://www.ps3mediaserver.org/forum/viewforum.php?f=8) on the
[forum](http://www.ps3mediaserver.org/forum/index.php)):

1\. manually convert the changelog to [Markdown](http://daringfireball.net/projects/markdown/) format.
This is straightforward as it's almost Markdown already:

Change:

		Engines:
			FFmpeg:
				- Add/fix audio channel options
				- Build script: add HTTPS support

		WEB.conf:
			- Add support for the file:// protocol
			- Match protocols (e.g. mms://...) before extensions (e.g. http://example.com/foo.mms)

		Misc:
			- Media Parser v1: fix audio channel parsing
			- Fix for "Can't assign requested address" bug after update to Java 1.6.0_45 on Mac OSX

To this:

	- Engines:
		FFmpeg:
			- Add/fix audio channel options
			- Build script: add HTTPS support

	- WEB.conf:
		- Add support for the file:// protocol
		- Match protocols (e.g. mms://...) before extensions (e.g. http://example.com/foo.mms)

	- Misc:
		- Media Parser v1: fix audio channel parsing
		- Fix for "Can't assign requested address" bug after update to Java 1.6.0_45 on Mac OSX

2\. Convert the Markdown to HTML with [pandoc](http://johnmacfarlane.net/pandoc/):

    pandoc -f markdown -t html changelog.txt > changelog.html

3\. Convert the HTML to BBCode with e.g. an online converter. This one works well:

http://www.seabreezecomputers.com/html2bbcode/

4\. Make sure there are no HTML-isms in the BBCode e.g. change HTML quote entities back to quotation marks &c.
