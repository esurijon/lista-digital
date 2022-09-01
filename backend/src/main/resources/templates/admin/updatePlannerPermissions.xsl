<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:util="xalan://com.surix.ld.util.XslExtensions"
  extension-element-prefixes="util"
   >

<xsl:param name="planner-id"/>
<xsl:param name="expiration-date"/>

<xsl:template match="/">
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="planner[@id=$planner-id]/permissions/permission[@type='writeEnabled']">
	<permission expires="{$expiration-date}" granted="true" type="writeEnabled"/>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>