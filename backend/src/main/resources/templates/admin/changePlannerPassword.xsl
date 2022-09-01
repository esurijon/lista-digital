<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="planner" />
  <xsl:param name="reset-token" />
  <xsl:param name="new-password" />

  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="password[../user=$planner and ../resetToken=$reset-token]">
    <password><xsl:value-of select="$new-password"></xsl:value-of></password>
  </xsl:template>

  <xsl:template match="resetToken[../user=$planner and .=$reset-token]">
    <resetToken>unset</resetToken>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>