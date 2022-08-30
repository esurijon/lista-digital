<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template name ="max">
  <xsl:param name ="list" />
  <xsl:choose >
  <xsl:when test ="$list">
    <xsl:variable name ="first" select ="$list[1]" />
  
    <xsl:variable name ="rest">
    <xsl:call-template name ="max">
      <xsl:with-param name ="list" select ="$list[position() != 1]" />
    </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
    <xsl:when test="$first &gt; $rest">
      <xsl:value-of select ="$first"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select ="$rest"/>     
    </xsl:otherwise>
    </xsl:choose>
  
  </xsl:when>
  <xsl:otherwise>0</xsl:otherwise>
  
  </xsl:choose>

</xsl:template>

</xsl:stylesheet> 