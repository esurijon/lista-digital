<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <xsl:apply-templates select="event/guests" />
  </xsl:template>
  
  <xsl:param name="sort-field">lastname</xsl:param>

	<xsl:template match="guests">

		<table id="plain-view">
			<thead>
        <tr>
          <td class="head0">LISTA DE INVITADOS</td>
          <td class="head1">APELLIDO</td>
          <td class="head2">NOMBRE</td>
          <td class="head3">CANT</td>
          <td class="head4">MESA</td>
          <td class="head5">CUBIERTO</td>
          <td class="head6">MENÚ</td>
          <td class="head7">SILLA BEBÉ</td>
          <td class="head8">COMENTARIOS</td>
          <td class="head9" title="Confirma asistencia">CONF</td>
        </tr>
			</thead>
			<tbody class="plain">
				<xsl:apply-templates select="guest[not(@state='D')]">
          <xsl:sort select="lastname" data-type="text" />
          <xsl:sort select="name" data-type="text" />
          <xsl:sort select="table" data-type="number" />
					<!-- 
          <xsl:sort select="(*|*/*)[name()=$sort-field]" data-type="text" />
           -->
 				</xsl:apply-templates>
			</tbody>
      <tfoot>
        <tr id="new">
        	<td colspan="9" class="new">+Agregar un invitado</td>
        </tr>
        <tr id="guestForm">
           <td colspan="9">
            <div>
              <table>
              <tr>
              <td style="width: 150px">
                <input class="field1" name="lastname" type="text" />
              </td>
              <td style="width: 192px">
                <input class="field2" name="name" type="text" />
              </td>
              <td style="width: 40px">
                <input class="field3" name="quantity" type="text" />
              </td>
              <td style="width: 40px">
                <select class="field4" name="table"></select>
              </td>
              <td style="width: 80px">
                <select class="field5" name="cubierto"></select>
              </td>
              <td style="width: 80px">
                <select class="field6" name="menu"></select>
              </td>
              <td style="width: 40px">
                <input class="field7" name="babychair" type="checkbox" value="true"/>
              </td>
              <td style="width: 220px">
                <input class="field8" name="comments" type="text" />
              </td>
              <td style="width: 40px">
                <input class="field4" name="confirmed" type="checkbox" value="true"/>
              </td>
              </tr>
              </table>
             </div>
            <div style="float: right;">
              <button name="remove" class="form-btn">ELIMINAR</button>
              <button name="copy" class="form-btn">COPIAR</button>
              <button name="insert" class="form-btn">AGREGAR</button>
              <button name="update" class="form-btn">ACEPTAR</button>
              <button name="cancelGuest" class="form-btn">CANCELAR</button>
            </div>
          </td>
        </tr> 
      </tfoot>
		</table>
	</xsl:template>

  <xsl:template match="guest">
    <tr>
      
      <xsl:attribute name="class">
        item <xsl:call-template name="zebra"/>
      </xsl:attribute>
      <xsl:attribute name="id">
        <xsl:value-of select="@id" />
      </xsl:attribute>
      <td name="full_name" class="col0">
        <input type="checkbox"/><xsl:value-of select="lastname" />, <xsl:value-of select="name" /> (<xsl:value-of select="quantity" />)
      </td>
      <td name="last_name" class="col1">
        <xsl:value-of select="lastname" />
      </td>
      <td name="name" class="col2">
        <xsl:value-of select="name" />
      </td>
      <td name="quantity" class="col3">
        <xsl:value-of select="quantity" />
      </td>
      <td name="table" class="col4">
        <xsl:variable name="table" select="table"/>
        <xsl:if test="$table > 0">
   	      <xsl:attribute name="title">
	          <xsl:value-of select="/event/tables/table[@id=$table]/description" />
   	      </xsl:attribute>
          <xsl:value-of select="table" />
        </xsl:if>
      </td>
      <td name="cubierto" class="col5">
        <xsl:value-of select="cubierto" />
      </td>
      <td name="menu" class="col6">
        <xsl:value-of select="menu" />
      </td>
      <td name="babychair" class="col7">
        <xsl:choose>
          <xsl:when test="babychair = 'true'">
            Si
          </xsl:when>
          <xsl:otherwise>
            No
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td name="comments" class="col8">
        <xsl:value-of select="comments" />
      </td>
      <td name="confirmed" class="col9">
        <xsl:choose>
          <xsl:when test="confirmed = 'true'">
            Si
          </xsl:when>
          <xsl:otherwise>
            No
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
