package payment.tools.system;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <pre>
 * Modify Information:
 * Author       Date          Description
 * ============ ============= ============================
 * liuzhicheng      2017年12月14日       加载系统变量
 * </pre>
 */
public class SystemEnvironment {

    // 饿汉式单例
    private static final SystemEnvironment instance = new SystemEnvironment();

    private String url;
    /**
     * svn地址
     */
    private String svnUrl;

    private String userName;

    private String password;

    private List<String> parentTemplateList = new ArrayList<String>();

    private List<String> commonTemplateList = new ArrayList<String>();

    private List<String> businessTemplateList = new ArrayList<String>();
    
    
    private Map<String,String> emailTempalteId = new HashMap<String, String>();
    
    private Map<String,String> prefixBranchesUrl = new HashMap<String, String>();

    private SystemEnvironment() {
    }

    public static final SystemEnvironment getInstance() {
        return instance;
    }

    public void initialize() {
        try {

            System.out.println("SystemEnvironment initialize start.");
            Properties properties = new Properties();
            properties.load(new FileInputStream("./config/jenkinsConfig.properties"));
            // InputStream in =
            // SystemEnvironment.class.getClassLoader().getResourceAsStream("jenkinsConfig.properties");
            // properties.load(in);

            url = properties.getProperty("jenkins.url");
            System.out.println("url:" + url);

            svnUrl = properties.getProperty("svn.url");
            System.out.println("svnUrl:" + svnUrl);

            userName = properties.getProperty("jenkins.userName");
            password = properties.getProperty("jenkins.password");

            String parentTemplate = properties.getProperty("xml.template.Parent");
            parentTemplateList = Arrays.asList(parentTemplate.split(","));

            String commonTemplate = properties.getProperty("xml.template.Common");
            commonTemplateList = Arrays.asList(commonTemplate.split(","));

            String businessTemplate = properties.getProperty("xml.template.Business");
            businessTemplateList = Arrays.asList(businessTemplate.split(","));

            //初始化邮件模板
            emailTempalteId.put("Department1", "emailext-template-1510644178438");
            emailTempalteId.put("Department2", "emailext-template-1510644328437");
            emailTempalteId.put("Dept3_Bank", "emailext-template-1510644443292");
            emailTempalteId.put("Dept3_DSP", "emailext-template-1510644381132");
            emailTempalteId.put("Dept3_TS", "emailext-template-1510644480721");
            
            prefixBranchesUrl.put("Department1", "D1_");
            prefixBranchesUrl.put("Department2", "D2_");
            prefixBranchesUrl.put("Dept3_Bank", "D3_Bank_");
            prefixBranchesUrl.put("Dept3_DSP", "D3_DSP_");
            prefixBranchesUrl.put("Dept3_TS", "D3_TS_");
            
            System.out.println("SystemEnvironment initialize end.");

        } catch (Exception e) {
            System.out.println("SystemEnvironment initialize Error" + e);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSvnUrl() {
        return svnUrl;
    }

    public void setSvnUrl(String svnUrl) {
        this.svnUrl = svnUrl;
    }

    public List<String> getParentTemplateList() {
        return parentTemplateList;
    }

    public List<String> getCommonTemplateList() {
        return commonTemplateList;
    }

    public List<String> getBusinessTemplateList() {
        return businessTemplateList;
    }

    public Map<String, String> getEmailTempalteId() {
        return emailTempalteId;
    }

    public Map<String, String> getPrefixBranchesUrl() {
        return prefixBranchesUrl;
    }
}
