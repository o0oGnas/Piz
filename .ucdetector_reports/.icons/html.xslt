<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
 Copyright (c) 2016 Joerg Spieler All rights reserved. This program and the
 accompanying materials are made available under the terms of the Eclipse
 Public License v1.0 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--  <xsl:strip-space elements="*"/>  -->
	<xsl:output encoding="UTF-8" indent="yes" method="html" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/transitional.dtd" />
	<xsl:template match="/">
		<xsl:comment>
 Copyright (c) 2016 Joerg Spieler All rights reserved. This program and the
 accompanying materials are made available under the terms of the Eclipse
 Public License v1.0 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
		</xsl:comment>
		
		<xsl:comment>
To create custom reports change:
ECLIPSE_HOME/dropins/org.ucdetector_*.jar/org/ucdetector/report/html.xslt
		</xsl:comment>
		
		<html>
			<head>
				<title>UCDetector Report</title>
				<link rel="icon" href=".icons/ucd.gif" type="image/gif"/>
			</head>
			<body>
				<h2 align="center">
					<a href="http://www.ucdetector.org/">
						<img src=".icons/ucdetector32.png" alt="UCDetector homepage" border="0"/>
					</a>
					<xsl:text>UCDetector Report</xsl:text>
				</h2>
				
				<table border="0"><!-- top table containing columns for about, preferences, searched in -->
					<tr>
					<!-- ================================================================
					 ABOUT
					 =============================================================== -->
						<td valign="top">
							<h3 align="center">About search</h3>
							<table border="1" style="empty-cells:show">
								<tr bgcolor="#C0C0C0">
									<th>Property</th>
									<th>Value</th>
								</tr>
								<xsl:for-each select="/ucdetector/statistics/abouts/about[@show = 'true']">
									<xsl:variable name="color">
										<xsl:choose>
											<xsl:when test="position() mod 2 = 0">#E6E6FA</xsl:when>
											<xsl:otherwise>#FFFACD</xsl:otherwise>
										</xsl:choose>
									</xsl:variable>
									<tr bgcolor="{$color}">
										<td>
											<xsl:value-of select="key"/>
										</td>
										<td>
											<xsl:value-of select="value"/>
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</td>
						<!-- ==============================================================
						PREFERENCES
						=============================================================== -->
						<td valign="top">
							<h3 align="center">Preferences</h3>
							<table border="1" style="empty-cells:show">
								<tr bgcolor="#C0C0C0">
									<th>Preference</th>
									<th>Value</th>
								</tr>
								<xsl:for-each select="/ucdetector/statistics/preferences/preference">
									<xsl:variable name="color">
										<xsl:choose>
											<xsl:when test="position() mod 2 = 0">#E6E6FA</xsl:when>
											<xsl:otherwise>#FFFACD</xsl:otherwise>
										</xsl:choose>
									</xsl:variable>
									<tr bgcolor="{$color}">
										<td>
											<xsl:value-of select="@key"/>
										</td>
										<td>
											<xsl:value-of select="@value"/>
										</td>
									</tr>
								</xsl:for-each>
							</table>
							<xsl:value-of select="count(/ucdetector/statistics/preferences/preference)"/> preferences above are different from default prefences.
						</td>
						<td valign="top">
						 <!-- ==============================================================
				     SEARCH IN
				     =============================================================== -->
							<h3 align="center">Searched in</h3>
							<table border="1" style="empty-cells:show">
								<tr bgcolor="#C0C0C0">
									<th>Element</th>
									<th>Type</th>
								</tr>
								<xsl:for-each select="/ucdetector/statistics/searched/search">
									<xsl:variable name="color">
										<xsl:choose>
											<xsl:when test="position() mod 2 = 0">#E6E6FA</xsl:when>
											<xsl:otherwise>#FFFACD</xsl:otherwise>
										</xsl:choose>
									</xsl:variable>
									<tr bgcolor="{$color}">
										<td>
											<xsl:value-of select="."/>
										</td>
										<td>
											<xsl:value-of select="@class"/>
										</td>
									</tr>
								</xsl:for-each>
							</table>
							Searched: <xsl:value-of select="count(/ucdetector/statistics/searched/search)"/>
						</td>
					</tr>
				</table><!-- top outer table -->
				
			  <!--	<xsl:value-of select="concat('Searched started: ', /ucdetector/statistics/dateStarted, '. Duration: ', /ucdetector/statistics/searchDuration)"/> -->
				<h3>Warnings</h3>
				<table border="1" style="empty-cells:show">
					<thead align="center">
						<tr bgcolor="#C0C0C0">
							<th>Nr</th>
							<th>Java</th>
							<th>Marker</th>
							<th>Description</th>
							<th>References**</th>
							<th>Author</th>
							<th>Location*</th>
							<th>Java type</th>
							<th>Marker type</th>
						</tr>
					</thead>
					<xsl:for-each select="/ucdetector/markers/marker">
						<xsl:variable name="color">
							<xsl:choose>
								<xsl:when test="position() mod 2 = 0">#E6E6FA</xsl:when>
								<xsl:otherwise>#FFFACD</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<tr bgcolor="{$color}">
							<!-- NR -->
							<td align="right">
								<xsl:value-of select="@nr"/>
							</td>
							<!-- JAVA TYPE -->
							<td align="center" valign="middle">
								<!--
                                <img src=".icons/Element{javaType/@simple}.gif" alt="{javaType/@long}" />
                                -->
								<img src=".icons/{javaType/@icon}" alt="{javaType/@long}" />
								<!--
								<xsl:text> </xsl:text>
								<xsl:value-of select="javaType/@long"/>
								-->
							</td>
							<!-- MARKER TYPE -->
							<td align="center" valign="middle">
								<img src=".icons/{@markerType}.gif" alt="{@markerType}" />
								<!--
								<xsl:text> </xsl:text>
								<xsl:value-of select="@markerType"/>
								-->
							</td>
							<!-- DESCRIPTION -->
							<td>
								<xsl:value-of select="description"/>
							</td>
							<!-- Reference Count -->
							<td align="right">
								<xsl:value-of select="@referenceCount"/>
							</td>
							<!-- Author -->
							<td align="right">
								<xsl:value-of select="author"/>
							</td>
							<!-- LOCATION -->
							<td>
  							<!--  org.eclipse.swt.SWT.error(SWT.java:3634) -->
			         <!-- For link in stack trace console space is needed here! -->
								<xsl:text>&#160;</xsl:text>
			         <!-- if not default package -->
								<xsl:if test="string-length(package) &gt; 0">
									<xsl:value-of select="concat(package, '.')"/>
								</xsl:if>

			          <!-- class name -->
								<xsl:value-of select="concat(class, '.')"/>

								<!-- class needs an additional string -->
								<xsl:if test="not(method) and not(field)">
									<xsl:text>declaration</xsl:text>
								</xsl:if>

								<!-- method -->
								<xsl:if test="method">
									<xsl:value-of select="method"/>
								</xsl:if>

								<!-- field -->
								<xsl:if test="field">
									<xsl:value-of select="field"/>
								</xsl:if>

								<!-- Link in Eclipse Stack Trace Console View: (SWT.java:3634) -->
								<xsl:value-of select="concat('(', class, '.java:', @line, ')')"/>
							</td>
							<!-- Java type -->
							<td align="right">
								<xsl:value-of select="javaType/@long"/>
							</td>
							<!-- Marker type -->
							<td align="right">
								<xsl:value-of select="@markerType"/>
							</td>
						</tr>
					</xsl:for-each>
				</table>

				<!-- ===================================================================
				     FOOTNODE
				     =============================================================== -->
				<p>
* To get links to the source locations, copy and paste first column (or table) to Eclipse 'Java Stack Trace Console'<br></br>
** Set 'Detect code with max number of references' &gt; 0<br></br>
				</p>

				<!-- ===================================================================
				     PROBLEMS
				     =============================================================== -->
				<xsl:if test="count(/ucdetector/problems/problem) &gt; 0">
					<h3>
						<font color="red">
							<xsl:value-of select="count(/ucdetector/problems/problem)"/> Exceptions found during detection</font>
					</h3>
					<ul>
						<xsl:for-each select="/ucdetector/problems/problem">
							<li>
								<b>
									<xsl:value-of select="status"/>
								</b>
								<pre>
									<font color="red">
										<xsl:value-of select="exception"/>
									</font>
								</pre>
							</li>
						</xsl:for-each>
					</ul>
				</xsl:if>
				<div align="right">
					<font color="#a0a0a0">
						<hr></hr>
						<xsl:text>Created with </xsl:text>
						<a href="http://www.ucdetector.org/">UCDetector
						<xsl:value-of select="/ucdetector/statistics/abouts/about[@name ='ucdetectorVersion']/value"/>
						</a>
					</font>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
<!-- :mode=xsl: -->
