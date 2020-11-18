//=========================================================================
//Copyright 2020 T-Mobile, US
//
//Licensed under the Apache License, Version 2.0 (the "License")
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//See the readme.txt file for additional language around disclaimer of warranties.
//=========================================================================
package com.tmobile.cso.vault.api.v2.controller;


import com.tmobile.cso.vault.api.exception.TVaultValidationException;
import com.tmobile.cso.vault.api.model.*;
import com.tmobile.cso.vault.api.service.SSLCertificateAWSRoleService;
import com.tmobile.cso.vault.api.service.SSLCertificateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@CrossOrigin
@Api( description = "SSL Certificate  Management Controller", position = 15)
public class SSLCertificateController {

	@Autowired
	private SSLCertificateService sslCertificateService;

	@Autowired
	private SSLCertificateAWSRoleService sslCertificateAWSRoleService;
	
	public static final String USER_DETAILS_STRING="UserDetails";

	/**
	 * To authenticate with Certificate Lifecycle Manager
	 * @param certManagetLoginRequest
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.login.value}", notes = "${SSLCertificateController.login.notes}", hidden = true)
	@PostMapping(value="/v2/auth/sslcert/login",produces="application/json")
	public ResponseEntity<String> authenticate(@RequestBody CertManagerLoginRequest certManagetLoginRequest) throws Exception {
		return sslCertificateService.authenticate(certManagetLoginRequest);
	}
	/**
	 * To Create SSL Certificate
	 * @param sslCertificateRequest
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.sslcreate.value}", notes = "${SSLCertificateController.sslcreate.notes}", hidden = true)
	@PostMapping(value="/v2/sslcert",consumes="application/json",produces="application/json")
	public ResponseEntity<String> generateSSLCertificate(HttpServletRequest request, @RequestHeader(value=
			"vault-token") String token,@Valid @RequestBody SSLCertificateRequest sslCertificateRequest)  {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.generateSSLCertificate(sslCertificateRequest,userDetails,token);
	}
	
	/**
	 * To get list of certificates in a container
	 * @param request
	 * @param token
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.getssl.value}", notes = "${SSLCertificateController.getssl.notes}")
	@GetMapping(value="/v2/sslcert", produces="application/json")
	public ResponseEntity<String> getCertificates(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestParam(name="certificateName", required = false) String certName,@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "offset", required = false) Integer offset, @RequestParam(name = "certType", required = true) String certType)throws Exception{
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.getServiceCertificates(token, userDetails, certName, limit, offset,certType);
     }
	
	/**
	 * To Get list of revocation reasons
	 * 
	 * @param request
	 * @param token
	 * @param certificateId
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.getRevocationReasons.value}", notes = "${SSLCertificateController.getRevocationReasons.notes}")
	@GetMapping(value = "/v2/certificates/{certificateId}/revocationreasons", produces = "application/json")
	public ResponseEntity<String> getRevocationReasons(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token, @PathVariable("certificateId") Integer certificateId) {
		return sslCertificateService.getRevocationReasons(certificateId, token);
	}
	
	/**
	 * Issue a revocation request for certificate
	 * 
	 * @param request
	 * @param token
	 * @param certificateId
	 * @param revocationRequest
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.issueRevocationRequest.value}", notes = "${SSLCertificateController.issueRevocationRequest.notes}", hidden = true)
	@PostMapping(value = "/v2/certificates/{certType}/{certName}/revocationrequest", produces = "application/json")
	public ResponseEntity<String> issueRevocationRequest(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token, @PathVariable("certType") String certType, @PathVariable("certName") String certName,
			@Valid @RequestBody RevocationRequest revocationRequest) {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.issueRevocationRequest(certType,certName, userDetails, token, revocationRequest);
	}
	
	/**
	 * Adds user with a read permission to a certificate
	 * @param token
	 * @param certificateUser
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.addUserToCertificate.value}", notes = "${SSLCertificateController.addUserToCertificate.notes}", hidden = true)
	@PostMapping(value="/v2/sslcert/user",consumes="application/json",produces="application/json")
	public ResponseEntity<String> addUserToCertificate(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestBody CertificateUser certificateUser){
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		boolean addSudoPermission = false;
		return sslCertificateService.addUserToCertificate(certificateUser, userDetails, addSudoPermission);
	}

	/**
	 * Adds a group to a certificate
	 * @param token
	 * @param CertificateGroup
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.addGroupToCertificate.value}", notes = "${SSLCertificateController.addGroupToCertificate.notes}", hidden = true)
	@PostMapping(value="/v2/sslcert/group",consumes="application/json",produces="application/json")
	public ResponseEntity<String> addGroupToCertificate(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestBody CertificateGroup certificateGroup){
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.addGroupToCertificate(userDetails, token,certificateGroup);
	}

	/**
	 * Get target system list.
	 * @param request
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "${CertificateController.getTargetSystemList.value}", notes = "${CertificateController.getTargetSystemList.notes}")
	@GetMapping(value = "/v2/sslcert/{certType}/targetsystems", produces = "application/json")
	public ResponseEntity<String> getTargetSystemList(HttpServletRequest request, @RequestHeader(value = "vault-token") String token, @PathVariable("certType") String certType) throws Exception {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.getTargetSystemList(token, userDetails, certType);
	}

	/**
	 * Get service list from a target system.
	 * @param request
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "${CertificateController.getTargetSystemServiceList.value}", notes = "${CertificateController.getTargetSystemServiceList.value}")
	@GetMapping(value = "/v2/sslcert/targetsystems/{targetsystem_id}/targetsystemservices", produces = "application/json")
	public ResponseEntity<String> getTargetSystemServiceList(HttpServletRequest request, @RequestHeader(value = "vault-token") String token, @PathVariable("targetsystem_id") String targetSystemId) throws Exception {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.getTargetSystemServiceList(token, userDetails, targetSystemId);
	}


    /**
     * Add approle to Certificate
     * @param request
     * @param token
     * @param certificateApprole
     * @return
     */
    @ApiOperation(value = "${SSLCertificateController.associateApproletoCertificate.value}", notes = "${SSLCertificateController.associateApproletoCertificate.notes}", hidden = false)
    @PostMapping(value="/v2/sslcert/approle",consumes="application/json",produces="application/json")
    public ResponseEntity<String> associateApproletoCertificate(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestBody CertificateApprole certificateApprole) {
        UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
        return sslCertificateService.associateApproletoCertificate(certificateApprole, userDetails);
    }

    /**
     * Delete approle from Certificate
     * @param request
     * @param token
     * @param certificateApproles
     * @return
     */
    @ApiOperation(value = "${SSLCertificateController.deleteApproleFromCertificate.value}", notes = "${SSLCertificateController.deleteApproleFromCertificate.notes}", hidden = false)
    @DeleteMapping(value="/v2/sslcert/approle",consumes="application/json",produces="application/json")
    public ResponseEntity<String> deleteApproleFromCertificate(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestBody CertificateApprole certificateApprole) {
        UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
        return sslCertificateService.deleteApproleFromCertificate(certificateApprole, userDetails);
    }
    
	/**
	 * Download certificate with private key.
	 * @param request
	 * @param token
	 * @param certificateDownloadRequest
	 * @return
	 */
	@ApiOperation(value = "${CertificateController.downloadCertificateWithPrivateKey.value}", notes = "${CertificateController.downloadCertificateWithPrivateKey.notes}", hidden = false)
	@PostMapping(value="/v2/sslcert/certificates/download", consumes="application/json")
	public ResponseEntity<InputStreamResource> downloadCertificateWithPrivateKey(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @Valid @RequestBody CertificateDownloadRequest certificateDownloadRequest) {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.downloadCertificateWithPrivateKey(token, certificateDownloadRequest, userDetails);
	}

	/**
	 * Download certificate.
	 * @param request
	 * @param token
	 * @param certificateName
	 * @param certificateType
	 * @return
	 */
	@ApiOperation(value = "${CertificateController.downloadCertificate.value}", notes = "${CertificateController.downloadCertificate.notes}", hidden = false)
	@GetMapping(value="/v2/sslcert/certificates/{certificate_name}/{certificate_type}/{sslcert_type}", produces=
			"application/json")
	public ResponseEntity<InputStreamResource> downloadCertificate(HttpServletRequest request, @RequestHeader(value=
			"vault-token") String token, @PathVariable("certificate_name") String certificateName, @PathVariable(
					"certificate_type") String certificateType,@PathVariable("sslcert_type") String sslCertType){
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.downloadCertificate(token, userDetails, certificateName, certificateType,sslCertType);
	}

	/**
	 * Get certificate details.
	 * @param request
	 * @param token
	 * @param certificateName
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.getCertificateDetails.value}", notes = "${SSLCertificateController.getCertificateDetails.notes}", hidden = true)
	@GetMapping(value = "/v2/sslcert/certificate/{certificate_type}", produces = "application/json")
	public ResponseEntity<String> getCertificateDetails(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token,
			@PathVariable("certificate_type") String certificateType,
			@RequestParam("certificate_name") String certificateName) {
		return sslCertificateService.getCertificateDetails(token, certificateName, certificateType);
	}

	 /**
	 * @param request
	 * @param token
	 * @param certificateId
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.renewCertificate.value}", notes = "${SSLCertificateController.renewCertificate.notes}", hidden = true)
	@PostMapping(value = "/v2/certificates/{certType}/{certName}/renew", produces = "application/json")
	public ResponseEntity<String> renewCertificate(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token, @PathVariable("certType") String certType, @PathVariable("certName") String certName) {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.renewCertificate(certType, certName, userDetails, token);
	}
	
	/**
	 * Removes permission for a user from the certificate
	 * @param request
	 * @param token
	 * @param certificateUser
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.removeUserFromCertificate.value}", notes = "${SSLCertificateController.removeUserFromCertificate.notes}", hidden = true)
	@DeleteMapping(value="/v2/sslcert/user", produces="application/json")
	public ResponseEntity<String> removeUserFromCertificate( HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestBody CertificateUser certificateUser){
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.removeUserFromCertificate(certificateUser, userDetails);
	}
	
	/**
     * Remove group from certificate
     * @param request
     * @param token
     * @param certificateGroup
     * @return
     */
    @ApiOperation(value = "${SSLCertificateController.removeGroupFromCertificate.value}", notes = "${SSLCertificateController.removeGroupFromCertificate.notes}", hidden = true)
    @DeleteMapping(value="/v2/sslcert/group", produces="application/json")
    public ResponseEntity<String> removeGroupFromCertificate( HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestBody CertificateGroup certificateGroup ){
        UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
        return sslCertificateService.removeGroupFromCertificate(certificateGroup, userDetails);
    }
    
	/**
	 * Get List Of Certificates
	 * 
	 * @param request
	 * @param token
	 * @param certificateType
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.getListOfCertificates.value}", notes = "${SSLCertificateController.getListOfCertificates.notes}")
	@GetMapping(value = "/v2/sslcert/certificates/{certificate_type}", produces = "application/json")
	public ResponseEntity<String> getListOfCertificates(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token,
			@PathVariable("certificate_type") String certificateType) {
		return sslCertificateService.getListOfCertificates(token, certificateType);
	}
	
	/**
	 * Transfer the ownership Of Certificates
	 * 
	 * @param request
	 * @param token
	 * @param certificateType
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.transferCertificateOwner.value}", notes = "${SSLCertificateController.transferCertificateOwner.notes}", hidden = true)
	@PutMapping(value = "/v2/sslcert/{certType}/{certName}/{certOwnerEmailId}/transferowner", produces = "application/json")
	public ResponseEntity<String> transferCertOwner(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token,  @PathVariable("certType") String certType, @PathVariable("certName") String certName,@PathVariable("certOwnerEmailId") String certOwnerEmailId) throws Exception {
		UserDetails userDetails = (UserDetails) request.getAttribute("UserDetails");
		return sslCertificateService.updateCertOwner(token, certType,certName,certOwnerEmailId, userDetails);
	}
	
	/**
	 * Transfer the ownership Of Certificates
	 * 
	 * @param request
	 * @param token
	 * @param certificateType
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.deleteCertificate.value}", notes = "${SSLCertificateController.deleteCertificate.notes}", hidden = true)
	@DeleteMapping(value = "/v2/certificates/{certificate_name}/{certificate_type}", produces = "application/json")
	public ResponseEntity<String> deleteCertificate(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token, @PathVariable("certificate_type") String certType, @PathVariable("certificate_name") String certName) throws Exception {
		UserDetails userDetails = (UserDetails) request.getAttribute("UserDetails");
		return sslCertificateService.deleteCertificate(token, certType, certName, userDetails);
	}

	/**
	 * Get the latest certificate details if approved
	 * @param certificateName
	 * @param certificateType
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.validateCertificateDetails.value}", notes = "${SSLCertificateController.validateCertificateDetails.notes}", hidden = true)
	@GetMapping(value = "/v2/sslcert/validate/{certificate_name}/{certificate_type}", produces = "application/json")
	public ResponseEntity<String> validateCertificateDetails(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token,
			@PathVariable("certificate_name") String certificateName,
			@PathVariable("certificate_type") String certificateType) {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.validateApprovalStatusAndGetCertificateDetails(certificateName, certificateType,
				userDetails);
	}
	
	/**
	 * To get list of internal certificates.
	 * @param request
	 * @param token
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.getAllCertificates.value}", notes = "${SSLCertificateController.getAllCertificates.notes}")
	@GetMapping(value="/v2/sslcert/list", produces="application/json")
	public ResponseEntity<String> getAllCertificates(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestParam(name="certificateName", required = false) String certName,@RequestParam(name = "limit", required = false) Integer limit, @RequestParam(name = "offset", required = false) Integer offset){
		return sslCertificateService.getAllCertificates(token, certName, limit, offset);
	}
	
	/**
	 * To get list of certificates based on certifcate Types for non-admin.
	 * @param request
	 * @param token
	 * @param certificateType
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.getAllCertificatesOnCertType.value}", notes = "${SSLCertificateController.getAllCertificatesOnCertType.notes}", hidden = true)
	@GetMapping(value = "/v2/sslcert/list/{certificate_type}", produces = "application/json")
	public ResponseEntity<String> getAllCertificatesOnCertType(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token,
			@PathVariable("certificate_type") String certificateType) {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.getAllCertificatesOnCertType(userDetails, certificateType);
	}
	
	/**	
	 * Check if status is revoked	
	 * @param certificateName	
	 * @param certificateType	
	 * @return	
	 */	
	@ApiOperation(value = "${SSLCertificateController.checkstatus.value}", notes = "${SSLCertificateController.checkstatus.notes}")	
	@GetMapping(value = "/v2/sslcert/checkstatus/{certificate_name}/{certificate_type}", produces = "application/json")	
	public ResponseEntity<String> checkCertificateStatus(HttpServletRequest request,	
			@RequestHeader(value = "vault-token") String token,	
			@PathVariable("certificate_name") String certificateName,	
			@PathVariable("certificate_type") String certificateType) {	
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);	
		return sslCertificateService.checkCertificateStatus(certificateName, certificateType,	
				userDetails);	
	}

	/**
	 * To get list of application names based on the self service groups.
	 *
	 * @param request
	 * @param token
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.getAllSelfServiceGroups.value}", notes = "${SSLCertificateController.getAllSelfServiceGroups.notes}", hidden = true)
	@GetMapping(value = "/v2/sslcert/grouplist", produces = "application/json")
	public ResponseEntity<String> getAllSelfServiceGroups(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token) {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.getAllSelfServiceGroups(userDetails);
	}
	
	/**
	 * To Onboard NCLM certificates to tvault
	 * @param request
	 * @param token
	 * @param clientId
	 * @param clientSecret
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "${SSLCertificateController.onboardcert.value}", notes = "${SSLCertificateController.onboardcert.notes}", hidden = true)
	@GetMapping(value="/v2/auth/sslcert/onboardcertificates",produces="application/json")
	public ResponseEntity<String> onboardCerts(HttpServletRequest request, @RequestHeader(value="vault-token") String token,
			@RequestParam(name = "from", required = false) Integer from, @RequestParam(name = "size", required = false) Integer size) throws Exception {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.onboardCerts(userDetails, token, from, size);
	}
	
	/**
	 * To Onboard single  NCLM certificates to tvault
	 * @param request
	 * @param token
	 * @param clientId
	 * @param clientSecret
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "${SSLCertificateController.onboardsinglecert.value}", notes = "${SSLCertificateController.onboardsinglecert.notes}", hidden = true)
	@GetMapping(value="/v2/auth/sslcert/onboardsinglecertificate",produces="application/json")
	public ResponseEntity<String> onboardSingleCertificate(HttpServletRequest request, @RequestHeader(value="vault-token") String token,
			@RequestParam(name = "certificateType", required = false) String certType, @RequestParam(name = "certificateName", required = false) String commonname,
			@RequestParam(name = "applicationName", required = false) String appTag, @RequestParam(name = "projectLeadEmailId", required = false) String projectLeadEmailId,
			@RequestParam(name = "notificationEmails", required = false) String notificationEmails) throws Exception {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.onboardSingleCert(userDetails, token, certType, commonname, appTag, notificationEmails, projectLeadEmailId);
	}

	/**
	 * Method to create an AWS ec2 role for SSL certificate
	 * @param token
	 * @param awsLoginRole
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.createAWSRoleForSSL.value}", notes = "${SSLCertificateController.createAWSRoleForSSL.notes}")
	@PostMapping(value="/v2/sslcert/aws/role",consumes="application/json",produces="application/json")
	public ResponseEntity<String> createAWSRoleForSSL(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestBody AWSLoginRole awsLoginRole) throws TVaultValidationException {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateAWSRoleService.createAWSRoleForSSL(userDetails, token, awsLoginRole);
	}

	/**
	 * Method to create AWS IAM role for SSL certificate
	 * @param token
	 * @param awsiamRole
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.createIAMRoleForSSL.value}", notes = "${SSLCertificateController.createIAMRoleForSSL.notes}")
	@PostMapping(value="/v2/sslcert/aws/iam/role",produces="application/json")
	public ResponseEntity<String> createIAMRoleForSSL(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @RequestBody AWSIAMRole awsiamRole) throws TVaultValidationException{
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateAWSRoleService.createIAMRoleForSSL(userDetails, token, awsiamRole);
	}

	/**
	 * Adds AWS role to SSL certificate
	 * @param token
	 * @param serviceAccountAWSRole
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.addAwsRoleToSSLCertificate.value}", notes = "${SSLCertificateController.addAwsRoleToSSLCertificate.notes}")
	@PostMapping (value="/v2/sslcert/aws",consumes="application/json",produces="application/json")
	public ResponseEntity<String> addAwsRoleToSSLCertificate(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @Valid @RequestBody CertificateAWSRole certificateAWSRole){
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateAWSRoleService.addAwsRoleToSSLCertificate(userDetails, token, certificateAWSRole);
	}

	/**
	 * Remove AWS role from SSL certificate
	 * @param token
	 * @param serviceAccountAWSRole
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.removeAWSRoleFromSSLCertificate.value}", notes = "${SSLCertificateController.removeAWSRoleFromSSLCertificate.notes}")
	@DeleteMapping (value="/v2/sslcert/aws",consumes="application/json",produces="application/json")
	public ResponseEntity<String> removeAWSRoleFromSSLCertificate(HttpServletRequest request, @RequestHeader(value="vault-token") String token, @Valid @RequestBody CertificateAWSRoleRequest certificateAWSRoleRequest){
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateAWSRoleService.removeAWSRoleFromSSLCertificate(userDetails, token, certificateAWSRoleRequest);
	}

	/**
	 * Delete/UnLink the certificate from application
	 * @param request
	 * @param token
	 * @param certificateName
	 * @param certificateType
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.unlinkCertificate.value}", notes = "${SSLCertificateController" +
			".unlinkCertificate.notes}", hidden = false)
	@PostMapping(value="/v2/sslcert/unlink/{certificate-name}/{certificate-type}",produces="application/json")
	public ResponseEntity<String> unlinkCertificate(HttpServletRequest request,
											@RequestHeader(value="vault-token") String token,
													@PathVariable(value="certificate-name") String certificateName,
													@PathVariable(value="certificate-type") String certificateType) throws Exception {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.unLinkCertificate(userDetails, token, certificateName,certificateType);
	}

	/**
<<<<<<< Upstream, based on 0511286aee5154d53d4e93f9a6ca066f4b8c953b
	 * Get all on-board pending certificates from nclm
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.getAllOnboardPendingCertificates.value}", notes = "${SSLCertificateController.getAllOnboardPendingCertificates.notes}")
	@GetMapping(value = "/v2/sslcert/pendingcertificates", produces = "application/json")
	public ResponseEntity<String> getAllOnboardPendingCertificates(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token) throws Exception { 
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.getAllOnboardPendingCertificates(token, userDetails);
	}


	/**
	 * To Onboard single NCLM certificates to tvault
	 *
	 * @param request
	 * @param token
	 * @param sslCertificateRequest
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "${SSLCertificateController.onboardSSLCertificate.value}", notes = "${SSLCertificateController.onboardSSLCertificate.notes}", hidden = true)
	@PostMapping(value = "/v2/sslcert/onboardSSLcertificate", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> onboardSSLCertificate(HttpServletRequest request,
			@RequestHeader(value = "vault-token") String token,
			@Valid @RequestBody SSLCertificateRequest sslCertificateRequest) throws Exception {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.onboardSSLcertificate(userDetails, token, sslCertificateRequest);
	}


	 /** To Update SSL Certificate metadata
	 * @param sslCertificateRequest
	 * @return
	 */
	@ApiOperation(value = "${SSLCertificateController.ssledit.value}", notes = "${SSLCertificateController.ssledit.notes}", hidden = false)
	@PutMapping(value="/v2/sslcert/",consumes="application/json",produces="application/json")
	public ResponseEntity<String> updateSSLCertificate(HttpServletRequest request, @RequestHeader(value=
			"vault-token") String token,@Valid @RequestBody CertificateUpdateRequest certificateUpdateRequest)  {
		UserDetails userDetails = (UserDetails) request.getAttribute(USER_DETAILS_STRING);
		return sslCertificateService.updateSSLCertificate(certificateUpdateRequest,userDetails,token);
	}

}