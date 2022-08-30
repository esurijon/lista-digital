<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:util="xalan://com.surix.ld.util.XslExtensions"
  extension-element-prefixes="util"
   >

<xsl:param name="event-id"/>
<xsl:param name="time-period"/>

<xsl:template match="/">
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="event[@id=$event-id]/permissions/permission[@type='writeEnabled' or @type='readEnabled']">
	<xsl:variable name="expiration-date" select="util:roll-date(util:max-date(@expires, util:current-date()),$time-period)"/>
	<permission expires="{$expiration-date}" granted="true" type="{@type}"/>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>