<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="query-string" />

  <xsl:template match="/">
    <xsl:apply-templates select="planners" />
  </xsl:template>
  
  <xsl:template match="planners">

    <table id="plain-view">
      <thead>
        <tr>
         <td class="head5">Ingreso</td>
         <td class="head1">Empresa</td>
         <td class="head2">E-mail</td>
         <td class="head4">Contacto</td>
         <td class="head3">Lectura</td>
         <td class="head3">Escritura</td>
         <td class="head6">VER</td>
        </tr>
      </thead>
      <tbody class="plain">
        <xsl:apply-templates select="planner[not(@state='D')]"/>
      </tbody>
      <tfoot>
        <tr id="new" style="width: 930px;">
          <td colspan="7" class="new">+Agregar un organizador</td>
        </tr>
      </tfoot>
    </table>
  </xsl:template>

  <xsl:template match="planner">
    <tr>
      <xsl:attribute name="class">
        item <xsl:call-template name="zebra"></xsl:call-template>
      </xsl:attribute>
      <xsl:attribute name="id">
        <xsl:value-of select="@id" />
      </xsl:attribute>
      <td name="type" class="col1 evt-type">
        <xsl:value-of select="registrationdate" />
      </td>
      <td name="date" class="col2 evt-date">
        <xsl:value-of select="name" />
      </td>
      <td name="date" class="col2 evt-date">
        <xsl:value-of select="user" />
      </td>
      <td name="hosts" class="col3 evt-hosts">
        <xsl:value-of select="contactPerson" />
      </td>
      <td name="hall" class="col4 evt-hall">
        <xsl:value-of select="permissions/permission[@type='readEnabled']/@expires" />
      </td>
      <td name="hall" class="col4 evt-hall">
        <xsl:value-of select="permissions/permission[@type='writeEnabled']/@expires" />
      </td>
      <td name="open" class="col5 evt-tool">
          <xsl:choose>
            <xsl:when test="@state = 'E' or @state = 'U'">
              <a target="_blank" href="/ld-static/planner/main.html?PLANNER_ID={@id}">Ver</a>
            </xsl:when>
            <xsl:otherwise>
              <a class="ver" target="_blank" href="unsaved">Ver</a>
            </xsl:otherwise>
          </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="zebra">
    <xsl:choose>
      <xsl:when test="position() mod 2 = 0">even</xsl:when>
      <xsl:otherwise>odd</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
