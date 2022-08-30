<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text" omit-xml-declaration="yes" media-type="text/csv"></xsl:output>

<xsl:template match="/">
  <xsl:apply-templates select="event/guests" />
</xsl:template>

<xsl:template match="guests">"APELLIDO","NOMBRE","CANTIDAD","MESA","CUBIERTO","MENÚ","SILLA BEBÉ","COMENTARIOS"
<xsl:apply-templates select="guest">
    <xsl:sort select="lastname" data-type="text" />
    <xsl:sort select="name" data-type="text" />
    <xsl:sort select="table" data-type="number" />
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="guest"><xsl:variable name="baby-chair">
  <xsl:choose>
    <xsl:when test="babychair = 'true'">Si</xsl:when>
  </xsl:choose>
</xsl:variable>"<xsl:value-of select="lastname" />","<xsl:value-of select="name" />","<xsl:value-of select="quantity" />","<xsl:value-of select="table" />","<xsl:value-of select="cubierto" />","<xsl:value-of select="menu" />","<xsl:value-of select="$baby-chair" />","<xsl:value-of select="comments" />"<xsl:text>
</xsl:text>
</xsl:template>

</xsl:stylesheet>
