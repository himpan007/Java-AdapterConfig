package com.nokia.netguard.adapter.test.suite.offline.requests.gs.resource.retrieval;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.api.responsedata.inventory.common.RawAttrsResourceResponseData;
import com.nakina.adapter.api.responsedata.inventory.common.RawAttrsResponseData;
import com.nakina.adapter.api.responsedatabuilder.ResponseDataBuilder;
import com.nakina.adapter.api.responsedatabuilder.nesecurity.resource.AccountCredentialBuilder;
import com.nakina.adapter.api.responsedatabuilder.nesecurity.resource.CredentialsResponseDataBuilder;
import com.nakina.adapter.api.responsedatabuilder.nesecurity.resource.LocalCredentialStoreResponseDataBuilder;
import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.resource.retrieval.RetrieveResourceRawAttributesRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;

import junit.framework.Assert;

@PrepareForTest({ LoginRequestHandler.class, AdapterConfigurationManager.class,
		RetrieveResourceRawAttributesRequestHandler.class, LogoutRequestHandler.class,
		Executor.class })
public class RetrieveResourceRawAttributesRequestHandlerTest extends AdapterJUnitBase {

	Vector<String> discoveredUsers = new Vector<String>();
	int NUMBER_OF_TEST_USERS       = 19;
	int NUMBER_OF_DATA_IN_SNAPSHOT = 29;

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public RetrieveResourceRawAttributesRequestHandlerTest(NE ne) {
		super(ne);
	}

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();

		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH,
				ne.getAdapterDefinitionDir());

		ignoreMockedCommandOrder();
		mockGSCommands();
	}

	@After
	public void destroy() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void retrivalTest() throws RequestFailure {
		RetrieveResourceRawAttributesRequestHandler handler = mockRequestHandler(new RetrieveResourceRawAttributesRequestHandler());
		List<ResponseDataBuilder> results = handler.getAllRawResourceData();

		assertEquals(NUMBER_OF_DATA_IN_SNAPSHOT, results.size());

		ResponseDataBuilder managedElement = results.get(0);
		assertEquals("ManagedElement", managedElement.getEntityId());
		assertEquals("Configuration", managedElement.getConfigResourceSubtype());

		checkGSResult(results, SV_86473r2, "SV-86473r2");
		checkGSResult(results, SV_86479r2, "SV-86479r2");
		checkGSResult(results, SV_86561r1, "SV-86561r1");
		checkGSResult(results, SV_86563r2, "SV-86563r2");
		checkGSResult(results, SV_86577r1, "SV-86577r1");
		checkGSResult(results, SV_86579r2, "SV-86579r2");
		checkGSResult(results, SV_86585r3, "SV-86585r3");
		checkGSResult(results, SV_86587r2, "SV-86587r2");
		checkGSResult(results, SV_86591r1, "SV-86591r1");
		checkGSResult(results, SV_86593r1, "SV-86593r1");
		checkGSResult(results, SV_86601r1, "SV-86601r1");
		checkGSResult(results, SV_86603r1, "SV-86603r1");
		checkGSResult(results, SV_86605r1, "SV-86605r1");
		checkGSResult(results, SV_86613r2, "SV-86613r2");
		checkGSResult(results, SV_86615r2, "SV-86615r2");
		checkGSResult(results, SV_86617r1, "SV-86617r1");
		checkGSResult(results, SV_86621r2, "SV-86621r2");
		checkGSResult(results, SV_86629r1, "SV-86629r1");
		checkGSResult(results, SV_86691r2, "SV-86691r2");
		checkGSResult(results, SV_86701r1, "SV-86701r1");
		checkGSResult(results, SV_86703r1, "SV-86703r1");
		checkGSResult(results, SV_86875r2, "SV-86875r2");
		checkGSResult(results, SV_86901r1, "SV-86901r1");
		checkGSResult(results, SV_86923r1, "SV-86923r1");
		checkGSResult(results, SV_86925r1, "SV-86925r1");		
		checkGSResult(results, SV_86927r2, "SV-86927r2");
		checkGSResult(results, SV_86937r1, "SV-86937r1");
		checkGSResult(results, GET_USERS_RESPONSE, "ManagedElement");	
		
		assertEquals(NUMBER_OF_ACCOUNTS, discoveredUsers.size());

	}

	private void checkGSResult(List<ResponseDataBuilder> results, String output, String entityId) {
		for (ResponseDataBuilder result : results) {
			if (result instanceof CredentialsResponseDataBuilder) {
				CredentialsResponseDataBuilder userData = (CredentialsResponseDataBuilder)result;
				List<RawAttrsResourceResponseData> builderList = userData.getRawAttrsResponseData();
				for (RawAttrsResourceResponseData builder : builderList) {
					String subType = builder.getConfigResourceSubType();
					if("account".equals(subType)){
						discoveredUsers.add(builder.getNativeName());
						Trace.info(getClass(), "Adding user "+builder.getNativeName());
					}					
				}				
				assertEquals(NUMBER_OF_TEST_USERS, discoveredUsers.size());
				for (int loop = 0; loop < NUMBER_OF_TEST_USERS; loop++) {					
					Trace.info(getClass(), "Checking for user "+discoveredUsers.get(loop));
					assertThat(output, containsString(discoveredUsers.get(loop)));
				}
				return;
				
			} else if (result.getEntityId().equals(entityId)) {
				assertEquals(1, result.getRawAttrsResponseData().size());
				for (String value : result.getRawAttrsResponseData().get(0).getAllRawAttrs().values()) {
					assertEquals(output, value);
					return;
				}
			}
		}

		Assert.fail("Entity: " + entityId + " not found in gs results");

	}

	private void mockGSCommands() throws Exception {
		addCommandMock("rpm -Va | grep '^.M'", SV_86473r2);
		addCommandMock("rpm -Va | grep '^..5'", SV_86479r2);
		addCommandMock("grep nullok /etc/pam.d/system-auth-ac", SV_86561r1);
		addCommandMock("grep -i '^PermitEmptyPasswords' /etc/ssh/sshd_config", SV_86563r2);
		addCommandMock("grep -i '^AutomaticLoginEnable' /etc/gdm/custom.conf", SV_86577r1);
		addCommandMock("grep -i '^TimedLoginEnable' /etc/gdm/custom.conf", SV_86579r2);
		addCommandMock("grep -i \"password_pbkdf2\" /boot/grub2/grub.cfg", SV_86585r3);
		addCommandMock("grep -i \"password\" /boot/efi/EFI/redhat/grub.cfg", SV_86587r2);
		addCommandMock("yum list installed rsh-server", SV_86591r1);
		addCommandMock("yum list installed ypserv", SV_86593r1);
		addCommandMock("grep ^gpgcheck /etc/yum.conf", SV_86601r1);
		addCommandMock("grep ^localpkg_gpgcheck /etc/yum.conf", SV_86603r1);
		addCommandMock("grep ^repo_gpgcheck /etc/yum.conf", SV_86605r1);
		addCommandMock("getenforce", SV_86613r2);
		addCommandMock("sestatus | grep \"Loaded policy name\" | awk -F ':' '{print $2}' | sed 's/^[ \\t]*//'",
				SV_86615r2);
		addCommandMock(
				"systemctl status ctrl-alt-del.service 2>&1 | grep 'Active:' | awk 'BEGIN {ORS=\" \"} {for (i=2; i<=NF; i++) print $i} {print \"\\n\"}'",
				SV_86617r1);
		addCommandMock("cat /etc/redhat-release", SV_86621r2);
		addCommandMock("awk -F ':' '$3 == 0 {print $1}' /etc/passwd", SV_86629r1);
		addCommandMock(
				"yum list installed dracut-fips 2>&1; grep fips /boot/grub2/grub.cfg; cat /proc/sys/crypto/fips_enabled",
				SV_86691r2);
		addCommandMock("yum list installed telnet-server", SV_86701r1);
		addCommandMock("systemctl is-active auditd.service", SV_86703r1);
		addCommandMock("grep -i ^Protocol /etc/ssh/sshd_config | grep -v '^\\#'", SV_86875r2);
		addCommandMock("find / -name '*.shosts' | awk '{print $0} END {if (!NF) print \"none\"}'", SV_86901r1);
		addCommandMock("yum list installed lftpd", SV_86923r1);
		addCommandMock("yum list installed tftp-server", SV_86925r1);
		addCommandMock("grep -i ^X11Forwarding /etc/ssh/sshd_config", SV_86927r2);
		addCommandMock(
				"if [ -f /etc/snmp/snmpd.conf ]; then grep public /etc/gdm/custom.conf | awk '{print $0} END {if (!NF) print \"public not found\"}' ; grep private /etc/gdm/custom.conf | awk '{print $0} END {if (!NF) print \"private not found\"}'; fi",
				SV_86937r1);
		addCommandMock("sudo lslogins --time-format=iso --noheadings -c -u -o=USER,UID,GECOS,HOMEDIR,GROUP,GID,SUPP-GROUPS,SUPP-GIDS,LAST-LOGIN,PWD-WARN,PWD-CHANGE,PWD-MIN,PWD-MAX,PWD-EXPIR,PROC", 
				GET_USERS_RESPONSE);
	}

	private static final String GET_USERS_RESPONSE = "root:0:root:/root:root:0:::::::::118\n" + 
			"nokadmin:1000::/home/nokadmin:nokadmin:1000:nokia:1001:2018-11-26T11:56:18+0000::::::18\n" + 
			"oracle:1001::/home/oracle:oinstall:101:nokia,dba,oracle:1001,102,103:2018-11-14T20:59:20+0000::::::113\n" + 
			"nokftp:1002::/home/nokftp:nokia:1001:::::::::0\n" + 
			"netadmin:1003::/home/netadmin:netadmin:1003:::::::::0\n" + 
			"acmuser:1004::/home/acmuser:acmuser:1004:::::::::0\n" + 
			"help:1005::/home/help:help:1005:::::::::0\n" + 
			"acmadmin:1006::/home/acmadmin:acmadmin:1006:::::::::0\n" + 
			"iamuser:1007::/home/iamuser:iamuser:1007:::2018-09-17T20:11:09+0000::::::0\n" + 
			"iamadmin:1008::/home/iamadmin:iamadmin:1008:::::::::0\n" + 
			"enduser:1009::/home/enduser:enduser:1009:::::::::0\n" + 
			"readonly:1010::/home/readonly:readonly:1010:::2018-10-18T20:39:46+0000::::::0\n" + 
			"iamresowner:1011::/home/iamresowner:iamresowner:1011:::::::::0\n" + 
			"iamuser2:1012::/home/iamuser2:iamuser2:1012:::::::::0\n" + 
			"readwrite:1013::/home/readwrite:readwrite:1013:::::::::0\n" + 
			"mona:1014::/home/mona:mona:1014:::2018-09-17T14:55:13+0000::::::0\n" + 
			"sergiop:1015::/home/sergiop:sergiop:1015:::2018-11-22T14:58:27+0000::::::0\n" + 
			"mgodin:1016::/home/mgodin:mgodin:1016:::::::::0\n" + 
			"iamuser3:1017::/home/iamuser3:iamuser3:1017:::2018-09-17T20:11:09+0000::::::0\n";
	private static final int    NUMBER_OF_ACCOUNTS = 19;
	
	private static final String SV_86927r2 = "X11Forwarding yes";
	private static final String SV_86703r1 = "active";
	private static final String SV_86691r2 = "Loaded plugins: fastestmirror\n"
			+ "Loading mirror speeds from cached hostfile\n" + " * base: ftp.funet.fi\n" + " * extras: ftp.funet.fi\n"
			+ " * updates: ftp.funet.fi\n" + "Error: No matching Packages to list\n" + "0";
	private static final String SV_86629r1 = "root";
	private static final String SV_86621r2 = "CentOS Linux release 7.2.1511 (Core)";
	private static final String SV_86615r2 = "argeted";
	private static final String SV_86613r2 = "Enforcing";
	private static final String SV_86601r1 = "gpgcheck=1";
	private static final String SV_86591r1 = "Loaded plugins: fastestmirror\n" + "Error: No matching Packages to list";
	private static final String SV_86587r2 = "grep: /boot/efi/EFI/redhat/grub.cfg: No such file or directory";
	private static final String SV_86577r1 = "grep: /etc/gdm/custom.conf: No such file or directory";
	private static final String SV_86563r2 = "";
	private static final String SV_86561r1 = "auth        sufficient    pam_unix.so nullok try_first_pass\n"
			+ "password    sufficient    pam_unix.so sha512 shadow nullok try_first_pass use_authtok";
	private static final String SV_86479r2 = "S.5....T.  c /etc/sudoers\n"
			+ ".M.......  g /etc/selinux/targeted/active/seusers\n"
			+ ".M.......    /etc/selinux/targeted/active/users_extra\n" + ".M.......  c /boot/grub2/grubenv\n"
			+ "S.5....T.  c /etc/bashrc\n" + "S.5....T.  c /etc/vsftpd/vsftpd.conf\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/java/cacerts\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/openssl/ca-bundle.trust.crt\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/pem/email-ca-bundle.pem\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/pem/objsign-ca-bundle.pem\n"
			+ ".M.......  g /etc/pki/ca-trust/extracteLoaded plugins: fastestmirror\n"
			+ "Error: No matching Packages to listd/pem/tls-ca-bundle.pem\n"
			+ "....L....  c /etc/pam.d/fingerprint-auth\n" + "....L....  c /etc/pam.d/password-auth\n"
			+ "....L....  c /etc/pam.d/postlogin\n" + "....L....  c /etc/pam.d/smartcard-auth\n"
			+ "....L....  c /etc/pam.d/system-auth\n" + ".M.......  c /etc/sysconfig/kernel\n"
			+ ".M.......  g /boot/initramfs-3.10.0-327.el7.x86_64.img\n" + "S.5....T.  c /etc/sysconfig/authconfig\n"
			+ ".M.......  g /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.151-1.b12.el7_4.x86_64/jre/lib/amd64/server/classes.jsa\n"
			+ ".M.......  c /etc/machine-id\n" + ".M.......  g /etc/udev/hwdb.bin\n"
			+ ".M.......  g /var/lib/systemd/random-seed\n" + "missing     /var/run/wpa_supplicant\n"
			+ "S.5....T.  c /etc/ssh/sshd_config";
	private static final String SV_86473r2 = "S.5....T.  c /etc/sudoers\n"
			+ ".M.......  g /etc/selinux/targeted/active/seusers\n"
			+ ".M.......    /etc/selinux/targeted/active/users_extra\n" + ".M.......  c /boot/grub2/grubenv\n"
			+ "S.5....T.  c /etc/bashrc\n" + "S.5....T.  c /etc/vsftpd/vsftpd.conf\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/java/cacerts\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/openssl/ca-bundle.trust.crt\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/pem/email-ca-bundle.pem\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/pem/objsign-ca-bundle.pem\n"
			+ ".M.......  g /etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem\n"
			+ "....L....  c /etc/pam.d/fingerprint-auth\n" + "....L....  c /etc/pam.d/password-auth\n"
			+ "....L....  c /etc/pam.d/postlogin\n" + "....L....  c /etc/pam.d/smartcard-auth\n"
			+ "....L....  c /etc/pam.d/system-auth\n" + ".M.......  c /etc/sysconfig/kernel\n"
			+ ".M.......  g /boot/initramfs-3.10.0-327.el7.x86_64.img\n" + "S.5....T.  c /etc/sysconfig/authconfig\n"
			+ ".M.......  g /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.151-1.b12.el7_4.x86_64/jre/lib/amd64/server/classes.jsa\n"
			+ ".M.......  c /etc/machine-id\n" + ".M.......  g /etc/udev/hwdb.bin\n"
			+ ".M.......  g /var/lib/systemd/random-seed\n" + "missing     /var/run/wpa_supplicant\n"
			+ "S.5....T.  c /etc/ssh/sshd_config";
	private static final String SV_86579r2 = SV_86577r1;
	private static final String SV_86585r3 = SV_86563r2;
	private static final String SV_86593r1 = SV_86591r1;
	private static final String SV_86603r1 = SV_86563r2;
	private static final String SV_86605r1 = SV_86563r2;
	private static final String SV_86617r1 = SV_86563r2;
	private static final String SV_86701r1 = SV_86591r1;
	private static final String SV_86875r2 = SV_86563r2;
	private static final String SV_86901r1 = SV_86563r2;
	private static final String SV_86923r1 = SV_86591r1;
	private static final String SV_86925r1 = SV_86591r1;
	private static final String SV_86937r1 = SV_86563r2;
}
