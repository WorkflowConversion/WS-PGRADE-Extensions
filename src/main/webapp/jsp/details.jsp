<!--
Workflow details main page
-->
<head>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:resourceURL var="ajaxURL" />
<script>

    var ajaxURL = "${ajaxURL}";
    var loadingconf_txt = '<msg:getText key="portal.config.loadingconfig" />';
</script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/asmlog.css?v=1"
      type="text/css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/facebox/facebox.css?v=1"
      type="text/css">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js">
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/facebox/facebox.js?v=1"></script>

<script type="text/javascript">

    function getLog(path) {
        $.ajax({
            type: "POST",
            url: path,
            success: function(data) {
                $(logHolder).html(data);
            }
        });
    }

</script>
</head>
<div id="rwlist" style="position:relative;">


    <portlet:defineObjects/>
    <portlet:actionURL var="pURL" portletMode="VIEW" />


    <table border="0" width="100%" >


        <tr><td>
                <div id="logHolder" name="logHolder"></div>
            </td></tr>
        <tr><td>
                <form method="post" action="${pURL}">
                    <input type="hidden" name="user_selected_instance" id="user_selected_Instance" value="${selected_Instance}">
                    <input type="hidden" name="guse" id="action">
                    <input type="hidden" name="action" id="action" value="doDetails">
                    <input type="submit" value="Refresh" class="portlet-form-button">
                </form>


            </td>
            <td>
                <form method="post" action="${pURL}">
                    <input type="hidden" name="guse" id="action">
                    <input type="hidden" name="action" id="action" value="doGoBack">
                    <input type="submit" value="Back" class="portlet-form-button">
                </form>
            </td>
        </tr>
        <tr>
            <td>
                <table width="100%" class="kback" border="1">
                    <tr align="center">
                        <td align="center"><strong>Job Name</strong></td>
                        <td align="center"><strong>Information</strong></td>
                    </tr>
                    <!--
        <tr>

        <td width="100%" colspan="4" style="border-bottom:solid 1px #ffffff;">
        <b><msg:getText key="text.wrkinst.instancename" /></b> ${rtid} </td>
    </tr>
                    -->
                    <c:forEach var="job" items="${workflow_details.jobs}" varStatus="ln">
                        <c:choose>
                            <c:when test="${(ln.index%2)==1}">
                                <c:set var="color" value="kline1" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="color" value="kline0" />
                            </c:otherwise>
                        </c:choose>

                        <tr>
                            <td class="${color}"> <strong>${job.name} </strong> </td>
                            <td class="${color}">
                                <table width="100%" class="kback">

                                    <!-- iterating over job instances according to their status-->
                                    <tr  align="center">
                                        <td colspan="5" align="center"> <strong>Status Overview for instances of Job "${job.name}" </strong></td>
                                    </tr>
                                    <tr>
                                        <td bgcolor="${statuscolors.statuscolors['INIT']}">INIT:${job.statisticsBean.numberOfJobsInInit}</td>
                                        <td bgcolor="${statuscolors.statuscolors['SUBMITTED']}">SUBMITTED:${job.statisticsBean.numberOfJobsInSubmitted}</td>
                                        <td bgcolor="${statuscolors.statuscolors['RUNNING']}">RUNNING:${job.statisticsBean.numberOfJobsInRunning}</td>
                                        <td bgcolor="${statuscolors.statuscolors['ERROR']}">ERROR:${job.statisticsBean.numberOfJobsInError}</td>
                                        <td bgcolor="${statuscolors.statuscolors['FINISHED']}">FINISHED:${job.statisticsBean.numberOfJobsInFinished}</td>
                                    </tr>
                                </table>
                                <hr>
                                <table  width="100%" class="kback">
                                    <tr align="center">
                                        <td colspan="4" align="center">
                                            <strong>Instances</strong>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td align="center">ID</td>
                                        <td align="center">Resource</td>
                                        <td align="center">Status</td>
                                        <td align="center">
                                            <table width="100%">
                                                <tr align="center">
                                                    <td  colspan="3" align="center">Actions</td>
                                                </tr>
                                                <tr align="center">
                                                    <td align="center"> Std. out</td>
                                                    <td align="center"> Std. err</td>
                                                    <td align="center"> LogBook</td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <c:forEach var="bjob" items="${job.instances}" varStatus="ln">

                                        <c:choose>
                                            <c:when test="${(ln.index%2)==1}">
                                                <c:set var="color" value="kline1" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="color" value="kline0" />
                                            </c:otherwise>
                                        </c:choose>
                                        <tr>
                                            <td class="${color}" width="10%">${bjob.pid} </td>
                                            <td class="${color}" width="30%">${bjob.usedResource}</td>
                                            <td width="25%" bgcolor="${statuscolors.statuscolors[statusconstants.statuses[bjob.status]]}">${statusconstants.statuses[bjob.status]} </td>
                                            <td>
                                                <c:if test="${(bjob.status==6) || (bjob.status==7)|| (bjob.status==9)}">
                                                    <table width="100%">
                                                        <tr align="center">

                                                            <td align="center">
                                                                <table>

                                                                    <tr>
                                                                        <td><input type="button" value="Show" onclick="getLog('${bjob.stdOutputPath}')"/></td>
                                                                        <td>
                                                                            <form method="post" action="${bjob.stdOutputPath}">
                                                                                <input type="submit" value="Download" class="portlet-form-button">
                                                                            </form>
                                                                        </td>

                                                                    </tr>
                                                                </table>
                                                            </td>
                                                            <td align="center">
                                                                <table>

                                                                    <tr>
                                                                        <td><input type="button" value="Show" onclick="getLog('${bjob.stdErrorPath}')"/></td>
                                                                        <td>
                                                                            <form method="post" action="${bjob.stdErrorPath}">
                                                                                <input type="submit" value="Download" class="portlet-form-button">
                                                                            </form>
                                                                        </td>

                                                                    </tr>
                                                                </table>
                                                            </td>
                                                            <td align="center">
                                                                <table>

                                                                    <tr>
                                                                        <td><input type="button" value="Show" onclick="getLog('${bjob.logBookPath}')"/></td>
                                                                        <td>
                                                                            <form method="post" action="${bjob.logBookPath}">
                                                                                <input type="submit" value="Download" class="portlet-form-button">
                                                                            </form>
                                                                        </td>

                                                                    </tr>
                                                                </table>
                                                            </td>
                                                            <!--<td>

                                                            </td>
                                                            <td>
                                                                <input type="button" value="Show Std. Output" onclick="getLog('${bjob.stdErrorPath}')"/>
                                                            </td>
                                                            <td>
                                                                <input type="button" value="Show Std. Output" onclick="getLog('${bjob.logBookPath}')"/>

                                                            </td>-->

                                                        </tr>
                                                    </table>
                                                </c:if>
                                            </td>
                                        </tr>

                                    </c:forEach>
                                </table>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </td>
        </tr>


    </table>

</div>
