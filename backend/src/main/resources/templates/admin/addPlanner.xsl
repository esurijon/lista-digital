<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:util="xalan://com.surix.ld.util.XslExtensions"
  extension-element-prefixes="util"
   >

<xsl:param name="today" />

<xsl:template match="/">
	<planners>
    <xsl:apply-templates/>
	</planners>
</xsl:template>

<xsl:template match="planner">
  <xsl:variable name ="id" select="@id"/>
  <xsl:variable name ="user" select="user"/>
  <xsl:variable name ="plnr" select="document('planners.xml.gz')/planners/planner[user = $user]"/>
  <xsl:if test="count($plnr) = 0">
    <planner state="N">
  		<xsl:apply-templates/>
  		<permissions>
	        <permission type="readEnabled" granted="true" expires="never"></permission>
	        <permission type="writeEnabled" granted="true" expires="{util:roll-date($today,'1M')}"></permission>
  		</permissions>
  		<registrationdate><xsl:value-of select="$today"/></registrationdate>
  	</planner>
  </xsl:if>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>