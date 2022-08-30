<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:template name="mail-headers"><![CDATA[Content-Type: text/html;

]]></xsl:template>

<xsl:variable name="h1">
  color:#33ADFF;
  font-family:Verdana,Arial,Helvetica,sans-serif;
  font-size:18px;
  font-weight: normal;
</xsl:variable>

<xsl:variable name="h2">
  color:#003A61; 
  font-family:'Trebuchet MS',arial,Calibri,sans-serif; 
  font-size:14px; 
  font-weight:bold;    
</xsl:variable>

<xsl:variable name="p">
  margin-bottom: 5px; 
  margin-left: 20px; 
  color: #333333; 
  font-family: 'verdana', arial, calibri, sans-serif; 
  font-size: 12px; 
  font-weight: normal; 
  line-height: 16px;    
</xsl:variable>

<xsl:variable name="li">
  color:#333333;
  font-family:verdana,arial,calibri,sans-serif;
  font-size:12px;
  font-weight:normal;
  line-height:16px;
  list-style-type:none;
  margin-bottom:5px;
  margin-left:20px;
  padding-left:30px;
</xsl:variable>

<xsl:variable name="button">
  background-color:#003A61;
  color:#ffffff;
  font-family:'Trebuchet MS',arial,Calibri,sans-serif;
  font-size:14px;
  font-weight:bold;
  padding:7px;
  text-align:center;
  text-decoration:none;
</xsl:variable>

<xsl:template name="header">
	<tr>
    <td align="center" style="border:0; padding: 0; margin: 0">
      <img src="http://www.listadigital.com.ar/ld-static/img/logo-mail.png"></img>
    </td>
	</tr>
</xsl:template>

<xsl:template name="body">
	<tr>
		<td style="min-height: 500px; padding-left: 20px;">
	   <xsl:apply-templates/>
		</td>
	</tr>
</xsl:template>

<xsl:template name="footer">
  <tr style="height:40px;">
    <td><![CDATA[ ]]></td>
  </tr>
	<tr style="background-color:#333333; bottom:0; height:30px; margin-top:20px;">
		<td style="color:#33ADFF; font-family:Verdana,Arial,Helvetica,sans-serif; font-size:18px; font-weight:normal;">
			<a style="margin: 8px; color:#33ADFF; display:block; font-family:Verdana,Arial,Helvetica,sans-serif; font-size:24px; line-height:25px;text-align:center; text-decoration:none;" href="http://www.listadigital.com.ar">
        www.listadigital.com.ar
      </a>
		</td>
	</tr>
</xsl:template>

<xsl:template name="email">
  <xsl:call-template name="mail-headers" />
  <table width="100%">
    <xsl:call-template name="header" />
    <xsl:call-template name="body" />
    <xsl:call-template name="footer" />
  </table>
</xsl:template>

<xsl:template match="/">
  <xsl:call-template name="email" />
</xsl:template>

</xsl:stylesheet>
