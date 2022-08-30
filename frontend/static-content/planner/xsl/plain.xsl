<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="query-string" />

  <xsl:template match="/">
    <xsl:apply-templates select="planner/events" />
  </xsl:template>
  
  <xsl:param name="sort-field">date</xsl:param>

	<xsl:template match="events">

		<table id="plain-view">
      <thead>
        <tr>
          <td class="head1">EVENTO</td>
          <td class="head2">FECHA</td>
          <td class="head3">ANFITRIONES</td>
          <td class="head4">SALÓN</td>
          <td class="head5">VER</td>
        </tr>
      </thead>
			<tbody class="plain">
				<xsl:apply-templates select="event[not(@state='D')]">
          <xsl:sort select="date" data-type="text" />
					<!-- 
          <xsl:sort select="(*|*/*)[name()=$sort-field]" data-type="text" />
           -->
 				</xsl:apply-templates>
			</tbody>
      <tfoot>
        <tr id="new" style="width: 930px;">
          <td colspan="5" class="new">+Agregar un evento</td>
        </tr>
      </tfoot>
		</table>
	</xsl:template>

  <xsl:template match="event">
    <tr>
      <xsl:attribute name="class">
        item <xsl:call-template name="zebra"></xsl:call-template>
      </xsl:attribute>
      <xsl:attribute name="id">
        <xsl:value-of select="@id" />
      </xsl:attribute>
      <td name="type" class="col1 evt-type">
        <xsl:value-of select="type" />
      </td>
      <td name="date" class="col2 evt-date">
        <xsl:value-of select="date" />
      </td>
      <td name="hosts" class="col3 evt-hosts">
        <xsl:apply-templates select="hosts/host"/>
      </td>
      <td name="hall" class="col4 evt-hall">
        <xsl:variable name="hall" select="hall"/>
        <xsl:value-of select="/planner/halls/hall[@id=$hall]/name" />
      </td>
      <td name="open" class="col5 evt-tool">
          <xsl:choose>
            <xsl:when test="@state = 'E' or @state = 'U'">
              <a class="ver" target="_blank">
                <xsl:attribute name="href">
                  <xsl:value-of select="concat('/ld-static/event/main.html?EVENT_ID=', @id, '&amp;', $query-string)"/>
                </xsl:attribute> 
                Ver
              </a>
            </xsl:when>
            <xsl:otherwise>
              <a class="ver" target="_blank" href="unsaved">Ver</a>
            </xsl:otherwise>
          </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="host">
	 <a class="mail">
      <xsl:attribute name="href">
        mailto:<xsl:value-of select="user" />
      </xsl:attribute>
   	  <xsl:value-of select="name" /><xsl:text> </xsl:text><xsl:value-of select="lastname" /> 
	 </a>, 
  </xsl:template>
  
  <xsl:template match="host[last()]">
	 <a class="mail">
      <xsl:attribute name="href">
        mailto:<xsl:value-of select="user" />
      </xsl:attribute>
   	  <xsl:value-of select="name" /><xsl:text> </xsl:text><xsl:value-of select="lastname" /> 
   </a> 
  </xsl:template>

  <xsl:template name="zebra">
    <xsl:choose>
      <xsl:when test="position() mod 2 = 0">even</xsl:when>
      <xsl:otherwise>odd</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
