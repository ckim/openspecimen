<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ page language="java" isELIgnored="false" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>	

<%@ page import="edu.wustl.catissuecore.actionForm.InstitutionForm"%>

<link rel="stylesheet" type="text/css" href="css/catissue_suite.css" />	
<script src="jss/ajax.js" type="text/javascript"></script>
<script type="text/javascript">
function addDepartment()
{
    var name = document.getElementById("name").value;
    var request = newXMLHTTPReq();
	if(request == null)
    {
		alert ("Your browser does not support AJAX!");
		return;
	}
	var handlerFunction = getReadyStateHandler(request,setDepartmentValues,true);
    request.onreadystatechange = handlerFunction;
	var param = "departmentName="+name;
    var url = "AddDepartment.do";
 	request.open("POST",url,true);
	
	request.setRequestHeader("Content-Type","application/x-www-form-urlencoded");	
	request.send(param);
	return false;
}

function setDepartmentValues(response)
{
    var values = response.split("#@#");
    if(values.length == 1)
	{
        var divTag = document.getElementById("errorDiv");
		divTag.innerHTML = "<font size='3' color='red'>"+values[0]  +"</font>"; 
	}
    else 
    {
   
		//Setting the ID
		deptCtrl = window.parent.document.getElementById("departmentId");
		deptCtrl.value = values[0];
	
	    //Seting the Department name
		displayDeptCtrl = window.parent.document.getElementById("displaydepartmentId");    
		displayDeptCtrl.value =values[1];

		//To hide the modal window
		parent.departmentWindow.hide();
	}

}

function cancelWindow()
{
  parent.departmentWindow.hide();
}
</script>

<!--begin content -->
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
<tr>
          <td> <div id="errorDiv"> </div></td>
	    </tr>
	<html:form action='/DepartmentAdd.do' onsubmit="return addDepartment()"> 
	<html:hidden property="operation" />
				<html:hidden property="submittedFor"/>
				<html:hidden property="id" />
  <tr>
    <td align="left"><table width="100%" border="0" cellpadding="3" cellspacing="0">
      <tr>
        <td><%@ include file="/pages/content/common/ActionErrors.jsp" %></td>
      </tr>
    </table></td>
  </tr>
  <tr>
    <td align="left" class="tr_bg_blue1"><span class="blue_ar_b">&nbsp;<bean:message key="user.department"/></span></td>
  </tr>
  <tr>
    <td align="left" class="showhide">
	<div id="part_det" >
      <table width="100%" border="0" cellpadding="3" cellspacing="2">
        
          <tr>
            <td width="1%" align="center" class="black_ar"><span class="blue_ar_b"><img src="images/uIEnhancementImages/star.gif" alt="Mandatory" width="6" height="6" hspace="0" vspace="0" /></span></td>
            <td width="12%" align="left" class="black_ar"><label for="name">
					<bean:message key="institution.name"/>
				</label></td>
            <td width="87%" align="left"><label>
              <html:text styleClass="black_ar" maxlength="255"  size="50" styleId="name" property="name"/>
            </label></td>
          </tr>
       
      </table>   </div> </td>
  </tr>
  
  <tr >
    <td class="buttonbg">
      <html:button property="clickButton" styleClass="blue_ar_b" onclick="addDepartment()" >
	      <bean:message  key="buttons.submit" /> 
      </html:button>
     
</td>
  </tr>
  </html:form>
</table><!--end content -->
