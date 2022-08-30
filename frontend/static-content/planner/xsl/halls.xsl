<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <div id="hallForm">
      <xsl:apply-templates select="planner/halls" />
    </div>
  </xsl:template>

  <xsl:template match="halls">
    <div id="hall-name">
      <table id="plain-view">
        <thead>
          <tr>
            <td width="350">SALÓN</td>
          </tr>
        </thead>
        <tbody>
          <xsl:apply-templates select="hall[not(@state='D')]">
            <xsl:sort select="name" />
          </xsl:apply-templates>
          <tr id="new">
            <td class="new">+Agregar salón</td>
          </tr>
          <tr id="hall-form" class="hidden hall-selected">
            <td>
              <div>
                <input type="hidden" name="plane" />
                <input type="text" name="name" class="hall-name" />
              </div>
              <div id="footer" style="float: right;">
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div id="hall-img">
      <form action="/ld/upload/plane" method="POST" enctype="multipart/form-data">
        <div class="left">
          <input type="radio" name="view" checked="checked" disabled="disabled" value="default" />
          <span>Imagen por defecto</span>
        </div>
        <div class="right">
          <input type="radio" name="view" disabled="disabled" value="custom" />
          <span>Subir plano:</span>
          <input name="plane-img" type="file" disabled="disabled" />
        </div>
        <img id="hall-plane" src="/planner-assets/hall.png"></img>
      </form>
    </div>
  </xsl:template>

  <xsl:template match="hall">
    <tr id="{@id}">
      <xsl:attribute name="class">
        item <xsl:call-template name="zebra"/>
      </xsl:attribute>
      <td>
        <xsl:value-of select="name" />
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