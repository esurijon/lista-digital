<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="templates.xsl"/>

<xsl:output method="html"></xsl:output>

<xsl:template match="data">
  <h1 style="{$h1}">
    Solicitud de cambio de contraseña.
  </h1>

  <p style="{$p}">
    Estimado usuario, tu solicitud de cambio de contraseña se ha procesado exitosamente.
  </p>
  <p style="text-align: center; margin: 50px;">
    <a style="{$button}">
      <xsl:attribute name="href"><xsl:value-of select="resetUrl"/></xsl:attribute>
      CAMBIAR MI CONTRASEÑA
    </a>
  </p>
  <p style="{$p}">
    Recuerda que para iniciar sesión en Lista Digital deberás identificarte indicando el nombre de Salón/Organizador y tu e-mail.
    <ul style="list-style: circle;">
      <li>Organizador: <b><xsl:value-of select="planner"/></b></li>
      <li>E-mail: <b><xsl:value-of select="user"/></b></li>
      <li>Contraseña: <b>****** (tu nueva contraseña)</b></li>
    </ul>
  </p>
  <p style="{$p}">
    En caso de que esta solicitud no haya sido requerida por usted simplemente elimine este correo, su contraseña permanecerá intacta.
  </p>
</xsl:template>

</xsl:stylesheet>
