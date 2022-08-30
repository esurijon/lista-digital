<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="templates.xsl"/>

<xsl:output method="html"></xsl:output>

<xsl:template match="data">
  <h1 style="{$h1}">
    Bienvenido a Lista Digital.
  </h1>

  <h2 style="{$h2}">
    Activación de cuenta:
  </h2>

  <p style="{$p}">
    Estimado usuario, tu Salón/Organizador de eventos <b><xsl:value-of select="planner"/></b>, te ha creado una cuenta en Lista Digital para que puedas comenzar a cargar la lista de invitados de tu evento.
  </p>
  <p style="{$p}">
    Lista Digital te ayuda con la pesada tarea de armar tu lista de invitados. Activá tu cuenta ahora y comenzá a disfrutar de todas las ventajas que te ofrece Lista Digital:
  </p>
  <p style="text-align: center; margin: 50px;">
    <a style="{$button}">
      <xsl:attribute name="href"><xsl:value-of select="activationUrl"/></xsl:attribute>
      ACTIVAR CUENTA
    </a>
  </p>
  <p style="{$p}">
    Recuerda que para iniciar sesión en Lista Digital deberás identificarte indicando el nombre de tu Salón/Organizador y tu e-mail.
    <ul style="list-style: circle;">
      <li>Organizador: <b><xsl:value-of select="planner"/></b></li>
      <li>E-mail: <b><xsl:value-of select="user"/></b></li>
      <li>Contraseña: <b>****** (la crearas al activar la cuenta)</b></li>
    </ul>
  </p>
</xsl:template>

</xsl:stylesheet>
