package payment.tools.system;

import com.offbytwo.jenkins.JenkinsServer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import payment.tools.util.StringUtil;
import payment.tools.util.XmlUtil;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * Modify Information:
 * Author       Date          Description
 * ============ ============= ============================
 * liuzhicheng      2017年12月14日       jenkins API 自动创建Job
 *
 * 要求jdk 版本1.7及以上
 *
 * </pre>
 */
public class JenkinsJobCreateServer {

    public static void main(String[] args) {

        try {
            // 参数顺序 1.department 2. 分支还是基线 3.modules

            if (args.length == 0) {
                throw new Exception("para is null");
            }

            // 开发室名称
            String viewName = args[0];
            // 系統初始化
            SystemEnvironment.getInstance().initialize();

            // jenkins服务器的地址
            String baseUrl = SystemEnvironment.getInstance().getUrl();

            // 存放需要创建的文件夹
            Map<String, String> folderMap = new HashMap<String, String>();
            // 存放job配置文件中svn的地址信息
            Map<String, String> jobConfigMap = new HashMap<String, String>();

            // 存放创建job是jenkins的job路径
            Map<String, String> jobInfoMap = appendPostUrl(args, folderMap, jobConfigMap);

            // 如果是创建分支，需要添加分支的文件夹
            if (folderMap.size() > 0) {
                for (Map.Entry<String, String> tmp : folderMap.entrySet()) {
                    String folderName = tmp.getKey();
                    System.out.println("Create Folder Name:" + folderName);
                    String serverUri = baseUrl + tmp.getValue();
                    try {
                        URI uri = new URI(serverUri);
                        JenkinsServer jenkinsServer = new JenkinsServer(uri, SystemEnvironment.getInstance().getUserName(), SystemEnvironment.getInstance()
                                .getPassword());
                        jenkinsServer.createFolder(folderName);
                    } catch (Exception e) {
                        System.out.println("Folder:" + folderName + " 已经存在");
                    }
                }
            }

            // 创建所需要的job
            for (Map.Entry<String, String> map : jobInfoMap.entrySet()) {

                String jobName = map.getKey();
                String realJobName = jobName + "_CI";
                String serverUri = baseUrl + map.getValue();

                String svnUrlTmp = jobConfigMap.get(jobName);

                System.out.println("Create Job Name:" + realJobName);
                System.out.println("Create Job ServerUri:" + realJobName);

                // 获取与之匹配的xml文件模板
                String xmlTemplate = getJobXmlTemplate(jobName);
                System.out.println("xmlTemplate:" + xmlTemplate);
                // 如果获取的模版类型为空，则跳过该模块
                if (xmlTemplate == null) {
                    continue;
                }
                Document document = XmlUtil.createDocument(new File("./config/xmlTemplate/jobTemplate_" + xmlTemplate + ".xml"));
                // 修改xml文件中的相关配置信息
                modifyXmlConfig(viewName, jobName, document, svnUrlTmp);

                String jobXml = XmlUtil.createPrettyFormat(document);

                URI uri = new URI(serverUri);
                JenkinsServer jenkinsServer = new JenkinsServer(uri, SystemEnvironment.getInstance().getUserName(), SystemEnvironment.getInstance()
                        .getPassword());
                if (jenkinsServer.getJob(realJobName) != null) {
                    System.out.println("jobName:" + realJobName + "已经存在");
                } else {
                    jenkinsServer.createJob(realJobName, jobXml);
                }

            }

        } catch (Exception e) {
            System.out.println("JenkinsJobCreateServer Error" + e);
        }

    }

    private static String getJobXmlTemplate(String jobName) {
        String jobModule = jobName.substring(jobName.indexOf("_") + 1);
        if (SystemEnvironment.getInstance().getParentTemplateList().contains(jobModule)) {
            return "Parent";
        } else if (SystemEnvironment.getInstance().getCommonTemplateList().contains(jobModule)) {
            return "Common";
        } else if (SystemEnvironment.getInstance().getBusinessTemplateList().contains(jobModule)) {
            return "Business";
        } else {
            System.out.println("jobModule:" + jobName + ",该模块在模板配置文件中不存在，请添加！");
            return null;
        }
    }


    /**
     * 修改新建模块的xml文件信息
     * @param viewName
     * @param jobName
     * @param document
     * @param svnUrl
     */
    private static void modifyXmlConfig(String viewName, String jobName, Document document, String svnUrl) {
        try {

            NodeList nodeList = document.getElementsByTagName("remote");
            NodeList nodeListDesc = document.getElementsByTagName("description");
            NodeList nodeListArtifactId = document.getElementsByTagName("artifactId");
            NodeList nodeListEmailTemplateId = document.getElementsByTagName("templateId");

            String emailTemplateId = SystemEnvironment.getInstance().getEmailTempalteId().get(viewName);

            for (int i = 0; i < nodeList.getLength(); i++) {
                // 设置job的svn路径

                nodeList.item(i).setTextContent(SystemEnvironment.getInstance().getSvnUrl() + svnUrl);
            }

            // 设置job的描述信息
            for (int i = 0; i < nodeListDesc.getLength(); i++) {
                nodeListDesc.item(i).setTextContent("svn/" + svnUrl + "持续集成任务");
            }

            // 设置ArtifactId
            for (int i = 0; i < nodeListArtifactId.getLength(); i++) {
                nodeListArtifactId.item(i).setTextContent(jobName.replace("_", "-"));
            }

            for (int i = 0; i < nodeListEmailTemplateId.getLength(); i++) {
                nodeListEmailTemplateId.item(i).setTextContent(emailTemplateId);
            }
        } catch (Exception e) {
            System.out.println("modifyXmlConfig Error" + e);
            ;
        }

    }


    /**
     * 拼接创建的文件夹以及job的url
     * @param args
     * @param folderMap
     * @param jobConfigMap
     * @return
     */
    private static Map<String, String> appendPostUrl(String[] args, Map<String, String> folderMap, Map<String, String> jobConfigMap) {

        Map<String, String> jobInfoMap = new HashMap<String, String>();

        String viewName = args[0];
        String branchName = args[1];
        String[] moduleName = args[2].split(",");
        String svnUrlPostfix = "@Head";

        for (int i = 0; i < moduleName.length; i++) {

            StringBuilder postfixUrl = new StringBuilder();
            String jobName = "";
            String svnUrl = "";
            String prefixBranch = "";

            if (StringUtil.isNotEmpty(viewName)) {
                postfixUrl.append("/view/").append(viewName);
                //根据viewName来判断分支的名字前缀，例如：D1_,D2_,D3_Bank_等
                prefixBranch = SystemEnvironment.getInstance().getPrefixBranchesUrl().get(viewName);
            }
            if (StringUtil.isNotEmpty(branchName)) {
                // 基线位置下
                if (branchName.contains("Base") && moduleName[i].contains("R91")) {
                    // 传入的moduleName[i]格式为
                    // 8001-MavenParent-R91,则tmp为8001-MavenParent
                    String tmp = moduleName[i].replace("-R91", "");
                    // jobName为Base_MavenParent
                    jobName = "Base_" + tmp.substring(tmp.indexOf("-") + 1);
                    postfixUrl.append("/job/").append(prefixBranch).append("Base_R91");
                    // svn路径为R91/Base_Maven/8001-MavenParent@Head
                    svnUrl = "R91/Base_Maven/" + tmp + svnUrlPostfix;
                } else if (branchName.contains("Base") && moduleName[i].contains("R93")) {
                    // 传入的moduleName[i]格式为
                    // 8006-BatchCommon-R93,则tmp为8006-BatchCommon
                    String tmp = moduleName[i].replace("-R93", "");
                    // jobName为Base_BatchCommon
                    jobName = "Base_" + tmp.substring(tmp.indexOf("-") + 1);
                    postfixUrl.append("/job/").append(prefixBranch).append("Base_R93");
                    // svn路径为R93/Base_Maven/8006-BatchCommon@Head
                    svnUrl = "R93/Base_Maven/" + tmp + svnUrlPostfix;
                } else {
                    // 传入的moduleName[i]格式为
                    // 8001-MavenParent-R91,则tmp为8001-MavenParent
                    String tmp = moduleName[i].substring(0, moduleName[i].lastIndexOf("-"));
                    // branchFolderName为B000-TestBranches-CI
                    String branchFolderName = branchName + "-CI";
                    // jobName 为 B000_MavenParent
                    jobName = branchName.substring(0, 4) + "_" + tmp.substring(tmp.indexOf("-") + 1);
                    // svn路径为R91/Branches/B000-TestBranches/8001-MavenParent@Head
                    svnUrl = moduleName[i].substring(moduleName[i].lastIndexOf("-") + 1) + "/Branches/" + branchName + "/" + tmp + svnUrlPostfix;
                    // folder Url 为/job/D1_Branches
                    postfixUrl.append("/job/").append(prefixBranch).append("Branches");
                    // 分支新建,folder创建
                    folderMap.put(branchFolderName, postfixUrl.toString());

                    // job URL为/job/D1_Branches/job/B000-TestBranches-CI
                    postfixUrl.append("/job/").append(branchFolderName);
                }
            }

            jobInfoMap.put(jobName, postfixUrl.toString());
            jobConfigMap.put(jobName, svnUrl);
        }

        return jobInfoMap;
    }

}
