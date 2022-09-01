<xsl:stylesheet version="1.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:template match="/">
  <html>
    <head>
      <script type="text/javascript" src="/ld-static/js/sarissa/sarissa.js"></script>
      <script>
        var parser = new DOMParser();
        var xmlString = '<xsl:apply-templates select="response/message"/>';
        var xml = parser.parseFromString(xmlString, "text/xml");  
        parent.uploadCallBack(xml);
      </script>
    </head>
    <body>
	   <xsl:apply-templates/>
    </body>
  </html>
</xsl:template>

<xsl:template match="*">
  <xsl:copy-of select="."/>
</xsl:template>

</xsl:stylesheet>