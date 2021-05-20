import org.json.simple.JSONObject;

public class ProjectInfo {

    private ProjectType projectType;
    private String downloadedDepFilePath;
    private String relativeDepFilePath;
    private JSONObject projectJSONData;
    private boolean innerProject;

    public ProjectInfo(String projectType, String downloadedDepFilePath, String relativeDepFilePath, JSONObject projectJSONData) {
        if (projectType.equals("maven")) {
            this.projectType = ProjectType.MAVEN;
        } else if (projectType.equals("gradle")) {
            this.projectType = ProjectType.GRADLE;
        }
        this.downloadedDepFilePath = downloadedDepFilePath;
        this.relativeDepFilePath = relativeDepFilePath;
        this.projectJSONData = projectJSONData;
        this.innerProject = true;
    }

    public Long getLastUpdated() {
        return (Long) this.projectJSONData.get("lastUpdated");
    }

    public String getRelativeDirectoryPath() {
        String[] splitPath = this.getRelativeDepFilePath().split("/");
        StringBuilder newRepositoryPathBuilder = new StringBuilder();
        newRepositoryPathBuilder.append("/");
        for (int i = 0; i < splitPath.length-1; i++) {
            newRepositoryPathBuilder.append(splitPath[i]);
        }
        return newRepositoryPathBuilder.toString();
    }

    public String getRepository() {
        return (String) this.projectJSONData.get("repository");
    }

    public String getUser() {
        return (String) this.projectJSONData.get("user");
    }

    public String getDefaultBranch() {
        return (String) ((JSONObject) this.projectJSONData.get("fullresponse")).get("default_branch");
    }

    public String getLink() {
        return (String) ((JSONObject) this.projectJSONData.get("fullresponse")).get("html_url");
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public String getDownloadedDepFilePath() {
        return downloadedDepFilePath;
    }

    public void setDownloadedDepFilePath(String downloadedDepFilePath) {
        this.downloadedDepFilePath = downloadedDepFilePath;
    }

    public String getRelativeDepFilePath() {
        return relativeDepFilePath;
    }

    public void setRelativeDepFilePath(String relativeDepFilePath) {
        this.relativeDepFilePath = relativeDepFilePath;
    }

    public JSONObject getProjectJSONData() {
        return projectJSONData;
    }

    public void setProjectJSONData(JSONObject projectJSONData) {
        this.projectJSONData = projectJSONData;
    }

    public boolean isInnerProject() {
        return innerProject;
    }

    public void setInnerProject(boolean innerProject) {
        this.innerProject = innerProject;
    }

    public ProjectInfo(ProjectType projectType, JSONObject projectJSONData) {
        this.projectType = projectType;
        this.projectJSONData = projectJSONData;
        this.innerProject = false;
    }

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "projectType=" + projectType +
                ", downloadedDepFilePath='" + downloadedDepFilePath + '\'' +
                ", relativeDepFilePath='" + relativeDepFilePath + '\'' +
                ", projectJSONData=" + projectJSONData +
                '}';
    }
}
