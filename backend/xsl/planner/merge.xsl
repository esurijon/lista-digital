<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:util="xalan://com.surix.ld.util.XslExtensions" extension-element-prefixes="util">

  <xsl:param name="planner-id" />
  <xsl:param name="planner-name" />

  <xsl:variable name="file-content">
  <![CDATA[<?xml version="1.0" encoding="UTF-8" ?><event><guests/><tables/></event>]]>
  </xsl:variable>

  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="events">
    <events>
      <xsl:variable name="ids" select="event[@state='U' or @state='D' or @state='N']/@id" />
      <xsl:for-each select="document(concat('/',$planner-id,'/events.xml.gz'))/planner/events/event[not(@id=$ids)]">
        <xsl:copy-of select="." />
      </xsl:for-each>

      <xsl:apply-templates select="event[@state='U' or @state='D' or @state='N']" />
    </events>
  </xsl:template>

  <!-- Updated nodes state should be set to 'E'-->
  <xsl:template match="event[@state='U']">
    <event id="{@id}" state="E">
      <xsl:apply-templates />
    </event>
  </xsl:template>

  <!-- Inserted nodes state should be set to 'E'-->
  <xsl:template match="event[@state='N']">
    <xsl:variable name="id" select="util:create-id()" />

    <event id="{$id}" state="E">
      <!-- Create event storage -->
      <xsl:variable name="create-storage-result" select="util:create-storage(concat($planner-id,'/',$id,'.xml.gz'),$file-content)" />
      <xsl:if test="string-length($create-storage-result)>0">
        <error type="CREATE_STORAGE_ERROR">
          <xsl:value-of select="$create-storage-result" />
        </error>
      </xsl:if>
      <xsl:choose>
      <xsl:when test="permissions">
	      <xsl:apply-templates select="permissions" />
      </xsl:when>
      <xsl:otherwise>
	      <permissions>
	        <permission type="readEnabled" granted="true" expires="never"></permission>
	        <permission type="writeEnabled" granted="true" expires="never"></permission>
	      </permissions>
      </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*[name() != 'permissions']" />
    </event>
  </xsl:template>

  <xsl:template match="event[@state='N']/hosts/host">
    <host type="{@type}">
      <xsl:variable name="token" select="util:create-token()" />
      <xsl:variable name="send-activate-host-account-mail-result" select="util:send-activate-host-account-mail(user, $planner-id, $planner-name, $token)" />
      <xsl:if test="string-length($send-activate-host-account-mail-result)>0">
        <error type="SEND_ACTIVATE_ACCOUNT_MAIL_ERROR">
          <xsl:value-of select="$send-activate-host-account-mail-result" />
        </error>
      </xsl:if>
      <password>
        <xsl:value-of select="$token" />
      </password>
      <resetToken>
        <xsl:value-of select="$token" />
      </resetToken>
      <xsl:apply-templates select="*[name() != 'password' or name() != 'resetToken']" />
    </host>
  </xsl:template>

  <!-- Deleted nodes may not be saved -->
  <xsl:template match="event[@state='D']">
    <xsl:variable name="delete-storage-result" select="util:delete-storage(concat($planner-id,'/',@id,'.xml.gz'))" />
    <xsl:if test="string-length($delete-storage-result)>0">
      <event id="{@id}" state="E">
        <error>
          <xsl:value-of select="$delete-storage-result" />
        </error>
        <xsl:apply-templates />
      </event>
    </xsl:if>
  </xsl:template>

  <xsl:template match="halls">
    <halls>
      <xsl:variable name="ids" select="hall[@state='U' or @state='D' or @state='N']/@id" />
      <xsl:for-each select="document(concat('/',$planner-id,'/events.xml.gz'))/planner/halls/hall[not(@id=$ids)]">
        <xsl:copy-of select="." />
      </xsl:for-each>

      <xsl:apply-templates select="hall[@state='U' or @state='D' or @state='N']" />
    </halls>
  </xsl:template>

  <!-- Updated nodes state should be set to 'E'-->
  <xsl:template match="hall[@state='U']">
    <hall id="{@id}" state="E">
      <xsl:apply-templates />
    </hall>
  </xsl:template>

  <!-- Inserted nodes state should be set to 'E'-->
  <xsl:template match="hall[@state='N']">
    <hall id="{@id}" state="E">
      <xsl:apply-templates />
    </hall>
  </xsl:template>

  <!-- Deleted nodes may not be saved -->
  <xsl:template match="hall[@state='D']" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
