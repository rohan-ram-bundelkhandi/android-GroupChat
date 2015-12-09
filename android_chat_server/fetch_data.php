<?php

 require_once('loader.php');

 $local_no_of_rows = $_REQUEST['data1'];
 
 $resultUsers =  getAllUsersChat($local_no_of_rows);

 if ($resultUsers != false)
	$NumOfUsers = mysql_num_rows($resultUsers);
 else
	$NumOfUsers = 0;

$jsonData      = array();

if ($NumOfUsers > 0) 
{
  	while ($rowUsers = mysql_fetch_array($resultUsers)) 
	{ 
		$jsonTempData = array();
		$jsonTempData['name']	      =$rowUsers["user_name"];
		$jsonTempData['imei']         = $rowUsers["user_imei"];
	 	$jsonTempData['message']      = $rowUsers["user_message"];
	 	$jsonTempData['time_stamp']   = $rowUsers["created_at"];
          	$jsonTempData['error']        = "n";
	   
	  $jsonData[] = $jsonTempData;	
	}  
}
else
{
	$jsonTempData = array();
	$jsonTempData['name']         = "Data not found.";
	$jsonTempData['imei']		="Data not found";
	$jsonTempData['message']        = "Data not found.";
  	$jsonTempData['time_stamp']         = "Data not found.";
  	$jsonTempData['error']        = "y";

   $jsonData[] = $jsonTempData;
}

   $outputArr = array();
   $outputArr['Android'] = $jsonData;
   
// Encode Array To JSON Data
   print_r( json_encode($outputArr));
?>
