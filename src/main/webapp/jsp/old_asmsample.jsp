<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:resourceURL var="resURL" />
<portlet:renderURL var="rURL" />
<portlet:actionURL var="uploadURL" />
<portlet:resourceURL var="ajaxURL" />
<portlet:resourceURL var="uploadStatusURL" >
<portlet:param name="sid" value="2778" />
<portlet:param name="uploadStatus" value="" />
</portlet:resourceURL>
<script>
    <!--
//    user='root';
    var ajaxURL = "${ajaxURL}";
    var uploadURL = "${uploadURL}";
    var uploadStatusURL = "${uploadStatusURL}";
    workflow = '${wrkdata.workflowID}';
    sjob = '';
    var sForm = 1;
    var portalID = "${portalID}";

    var userID = "${userID}";
    var workflowID = "${wrkdata.workflowID}";
    var confID = "${confID}";
    var jobID = "";
    var vJob = "";
    var callflag = 0;
    var fileUploadErrorFlag = 0;
    var formid = 0;
    var action = ""
//    var sid0=document.cookie.split("=");
//-->





    function getY(el) {
        var ret = 0;
        while (el != null) {
            ret += el.offsetTop;
            el = el.offsetParent;
        }
        return ret;
    }

    function setSubmitMenu(whichDiv, whichRef) {
        document.getElementById(whichDiv).style.top = getY(whichRef) - 50;
    }


</script>




<style type="text/css">
    div.draggable {
        position: absolute;
        top: 300px;
        left: 400px;
        z-index:100;
        background-color: #ccc;
        border: 1px solid #000;
        opacity: 0.9;
        padding: 5px;
        filter: alpha(opacity=90);
        display: none;
    }
    div.draggable div {
        background-color: #ffa;
        z-index:101;
        opacity: 1.0;
        color: #000;
        padding: 5px;
        filter: alpha(opacity=100);
    }
</style>
<portlet:defineObjects/>

<portlet:actionURL var="pURL" portletMode="VIEW" />

<div class="draggable" id="div_notify">
    <form method="post" action="${pURL}">
        <table><tr>
                <td>Write some words about the submmision: </td>
                <td>
                    <input type="hidden" name="action" id="action" value="doSubmit">
                    <input type="hidden" id="selected_workflow" name="selected_workflow"/>
                    <input type="text" name="notifyText" size="30" value="some text..."/>
                </td>
            </tr>
            <tr>
                <td>Set notification type:</td>
                <td>
                    <select id="notifyType" name="notifyType">";
                        <option value=""> Never</option>";
                        <option value="chg">On every status change</option>";
                        <option value="end">On terminated states (Finished or Error)</option>";
                    </select>";
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" value="Start!" class="portlet-form-button">
                    <input type="button" value="Cancel" class="portlet-form-button" onClick="javascript:document.getElementById('div_notify').style.display = 'none';"/>
                </td>
            </tr>
        </table>
    </form>
</div>



<div id="div_getworkflowsrep">
    <form id="confform" method="post" action="${pURL}" >
        <input type="hidden" name="action" id="action" value="doGetWorkflowsFromRepository">

        <table>
            <tr>
                <td>Get workflows exported by(Developer ID) :</td>
                <td>

                    <select id="owner" name="owner">
                        <c:forEach var="own" items="${owners}">
                            <option>
                                ${own}
                            </option>
                        </c:forEach>
                    </select>
                </td>
                <td>
                    <input type="submit" value="List Workflows from repository" class="portlet-form-button">


                </td>
            </tr>
        </table>
    </form>
</div>

<div id="div_import">
    <form method="post" action="${pURL}" >
        <input type="hidden" name="action" id="action" value="doImportWorkflow">
        <input type="hidden" name="rep_owner" id="rep_owner" value="${rep_owner}">
        <c:if test="${fn:length(WorkflowList) > 0}">
            <br>
            <hr/>




            <br>
            <table width="100%">
                <tr>
                    <td>
                        <strong><i>Select</i></strong>
                    </td>
                    <td>
                        <strong><i>Workflow Name</i></strong>
                    </td>
                    <td>
                        <strong><i>Workflow Type</i></strong>
                    </td>
                </tr>

                <c:forEach var="wf" items="${WorkflowList}">
                    <tr>
                        <td>
                            <input type="radio" name="impItemId" value="${wf.id}" />
                        </td>
                        <td>
                            ${wf.itemID}
                        </td>
                        <td>
                            ${wf.exportType}
                        </td>
                    </tr>
                </c:forEach>
                <tr>
                    <td>
                        <input type="submit" value="Import selected workflow" class="portlet-form-button">
                    </td>
                </tr>
            </table>
        </c:if>
    </form>
</div>

<br>
<hr/>
<br>
<table width="100%" border="1">
    <tr>
        <td align="center">
            <strong><i>Workflow Name</i></strong>
        </td>
        <td align="center">
            <strong><i>Submission note</i></strong>
        </td>
        <td align="center">
            <strong><i>Workflow Status</i></strong>
        </td>
        <td align="center" colspan="2">
            <strong><i>Configuration Actions</i></strong>
        </td>
        <td align="center" colspan="5">
            <strong><i>Management Actions</i></strong>
        </td>
    </tr>
    <c:forEach var="workflows" items="${asm_instances}">
        <tr>

            <td align="center">
                ${workflows.workflowName}
            </td>
            <td align="center">
                ${workflows.submissionText}
            </td>

            <td bgcolor="${workflows.statusbean.color}" align="center">
                ${workflows.statusbean.status}
            </td>
            <td>
                <form method="post" action="${pURL}" >
                    <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${workflows.workflowName}"/>




                    <select id="action" name="action">

                        <option value="doGetInput">Get Command Line Text</option>
                        <option value="doSetInput">Set Command Line Text</option>

                        <option value="doGetInputValue">Get Value set on port X</option>
                        <option value="doSetInputValue">Set Value set on port X</option>

                        <option value="doGetSQLQuery">Get SQL Query(url;username;password;query)</option>
                        <option value="doSetSQLQuery">Set SQL Query(url;username;password;query)</option>

                        <option value="doGetRemoteOutputPath">Get Remote Output Path</option>
                        <option value="doSetRemoteOutputPath">Set Remote Output Path</option>

                        <option value="doGetResource">Get a Job's Resource</option>
                        <option value="doSetResource">Set a Job's Resource</option>

                        <option value="doGetNodeNumber">Get Number of required processes(MPI)</option>
                        <option value="doSetNodeNumber">Set Number of required processes(MPI)</option>

                        <option value="doGetNumberOfInputs">Get Number of Input Files</option>
                        <option value="doSetNumberOfInputs">Set Number of Input Files</option>

                        <option value="doGetRequirements">Get Requirements set in JDL</option>
                        <option value="doSetRequirements">Set Requirements set in JDL</option>

                    </select>
                    <c:choose>
                        <c:when test="${act_workflowID eq workflows.workflowName}">
                            <input type="text" id="content" name="content" value="${content}"/>
                        </c:when>
                        <c:otherwise>
                            <input type="text" id="content" name="content" value=""/>
                        </c:otherwise>
                    </c:choose>
                    <input type="submit" value="Apply" class="portlet-form-button">
                </form>

            </td>
            <td>
                <input type="button" value="Upload Input" class="portlet-form-button"
                       onClick="javascript:document.getElementById('instance_upload_${workflows.workflowName}').value = '${workflows.workflowName}';
                               setSubmitMenu('div_upload_${workflows.workflowName}', this);
                               document.getElementById('div_upload_${workflows.workflowName}').style.display = 'block';
                       "/>
            </td>

            <td>
                <form method="post" action="${pURL}" >
                    <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${workflows.workflowName}"/>
                    <input type="hidden" name="action" id="action" value="doSubmit">
                    <%--<lpds:submit actionID="action" actionValue="doSubmit" cssClass="portlet-form-button" txt="Submit!" tkey="true"/>--%>
                    <input type="button" value="Start" class="portlet-form-button"
                           onClick="javascript: document.getElementById('selected_workflow').value = '${workflows.workflowName}';
                                   setSubmitMenu('div_notify', this);
                                   document.getElementById('div_notify').style.display = 'block';
                           ">
                </form>

            </td>

            <td align="center">
                <form method="post" action="${pURL}" >
                    <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${workflows.workflowName}"/>
                    <input type="hidden" name="action" id="action" value="doDetails">
                    <input type="submit" value="Details" class="portlet-form-button">
                </form>
            </td>


            <td>
                <input type="button" value="Download" class="portlet-form-button"
                       onClick="javascript:document.getElementById('workflow_download_${workflows.workflowName}').value = '${workflows.workflowName}';
                               javascript:document.getElementById('output_download_${workflows.workflowName}').value = '${workflows.workflowName}';
                               javascript:document.getElementById('file_download_${workflows.workflowName}').value = '${workflows.workflowName}';
                               setSubmitMenu('div_download_${workflows.workflowName}', this);
                               document.getElementById('div_download_${workflows.workflowName}').style.display = 'block';
                       "/>
            </td>
            <td>

            </td>
            <td>
                <form method="post" action="${pURL}" >
                    <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${workflows.workflowName}"/>
                    <input type="hidden" name="action" id="action" value="doDelete">
                    <input type="submit" value="Delete" class="portlet-form-button">
                </form>

            </td>

        </tr>
    </c:forEach>
</table>


<hr/>

<c:forEach var="inst" items="${asm_instances}">
    <div class="draggable" id="div_upload_${inst.workflowName}">
        <form method="post" action="${pURL}" enctype="multipart/form-data">
            <input type="hidden" name="action" id="action" value="doUpload">
            <input type="hidden" id="instance_upload_${inst.workflowName}" name="instance_upload_${inst.workflowName}"/>
            <table>
                <tr>
                    <td>
                        Select job and port:
                    </td>
                    <td>
                        <select id="where2upload" name="where2upload">
                            <%-- getting selected workflows' joblist --%>

                            <c:forEach var="jobs" items="${inst.jobs}">

                                <c:forEach var="ports" items="${jobs.value.input_ports}">
                                    <option value="${jobs.key}@${ports.key}@${ports.value}">
                                        ${jobs.key} job's ${ports.key} 's port ( ${ports.value} )
                                    </option>
                                </c:forEach>
                            </c:forEach>
                        </select>
                    </td>
                </tr>

                <tr>
                    <td>
                        <input type="file" name="input_2788_file" id="input_2788_file"/>
                        <input type="submit" value="Upload!" />
                        <input type="button" value="Cancel" class="portlet-form-button" onClick="javascript:
                                               document.getElementById('div_upload_${inst.workflowName}').style.display = 'none';"/>
                    </td>
                </tr>
            </table>
        </form>
    </div>
</c:forEach>

<c:forEach var="inst" items="${asm_instances}">
    <div class="draggable" id="div_download_${inst.workflowName}">
        <table>
            <tr>
                <td align="center">
                    <form method="post" action="${resURL}">
                        <input type="hidden" id="workflow_download_${inst.workflowName}" name="workflow_download_${inst.workflowName}" value="${workflows.workflowName}"/>
                        <input type="submit" value="Download Workflow!" class="portlet-form-button">
                    </form>    
                </td>
            </tr>
            <tr>
                <td align="center">
                    <form method="post" action="${resURL}">
                        <input type="hidden" id="output_download_${inst.workflowName}" name="output_download_${inst.workflowName}" value="${inst.workflowName}"/>
                        <input type="submit" value="Download Outputs!" class="portlet-form-button">

                    </form>
                </td>
            </tr>
            <tr>
                <td align="center">
                    <form method="post" action="${resURL}">
                        <input type="hidden" id="file_download_${inst.workflowName}" name="file_download_${inst.workflowName}" value="${inst.workflowName}"/>

                        <select id="file2download" name="file2download">
                            <%-- getting selected workflows' joblist --%>
                            <c:forEach var="jobs" items="${inst.jobs}">

                                <c:forEach var="ports" items="${jobs.value.output_ports}">
                                    <option value="${jobs.key}@${ports.key}@${ports.value}">
                                        ${jobs.key} job's ${ports.key} 's port ( ${ports.value} )
                                    </option>
                                </c:forEach>
                            </c:forEach>
                        </select>
                        <input type="submit" value="Download a File!" class="portlet-form-button">


                    </form>
                </td>
            </tr>
            <tr>
                <td align="center">
            <input type="button" value="Cancel" class="portlet-form-button" onClick="javascript:
                                document.getElementById('div_download_${inst.workflowName}').style.display = 'none';"/>
                </td>
            </tr>
        </table>


        <!--
                <form method="post" action="${resURL}">
                    <input type="hidden" id="instance_download_${inst.workflowName}" name="instance_download_${inst.workflowName}" value="${inst.workflowName}"/>
        
                    <table>
                        <tr>
        
                            
                            <td>
                                <select id="workflowtype" name="workflowtype">
                                    <option value="alloutputs"> Outputs </option>
                                    <option value="alloutputs_beta"> Outputs(Beta) </option>
                                    <option value="justlogs"> Logs only </option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                OR Select job and port:
                            </td>
                            <td>
                                <select id="file2download" name="file2download">
        <%-- getting selected workflows' joblist --%>
        <c:forEach var="jobs" items="${inst.jobs}">

            <c:forEach var="ports" items="${jobs.value.output_ports}">
                <option value="${jobs.key}@${ports.key}@${ports.value}">
                ${jobs.key} job's ${ports.key} 's port ( ${ports.value} )
            </option>
            </c:forEach>
        </c:forEach>
    </select>
</td>
</tr>

<tr>
<td>
    <input type="submit" value="Download!" class="portlet-form-button">
           <input type="button" value="Cancel" class="portlet-form-button" onClick="javascript:
document.getElementById('div_download_${inst.workflowName}').style.display = 'none';"/>
</td>
</tr>
</table>

</form>
        -->
    </div>
</c:forEach>
