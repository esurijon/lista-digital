<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output encoding="UTF-8" />

<xsl:template match="/">
		<xsl:apply-templates />
</xsl:template>

<xsl:template match="event[@state='E']">
</xsl:template>

<xsl:template match="hall[@state='E']">
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>