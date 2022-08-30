<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="planner" />
  <xsl:param name="event-id" />

  <xsl:template match="/">
    <xsl:apply-templates select="/planner/events/event[@id=$event-id]"/>
  </xsl:template>

  <xsl:template match="event">
	<event id="{@id}">
		<planner>
			<xsl:value-of select="$planner"/>
		</planner>
    <xsl:apply-templates/>
	</event>
  </xsl:template>

  <xsl:template match="password">
  </xsl:template>

  <xsl:template match="resetToken">
  </xsl:template>

  <xsl:template match="event/hall">
      <xsl:variable name="hall" select="string(.)"/>
      <xsl:apply-templates select="/planner/halls/hall[@id = $hall]"/>
  </xsl:template>

  <xsl:template match="@*|node()">
	<xsl:copy>
	  <xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
  </xsl:template>

</xsl:stylesheet>