package com.example.gitlab_monitor.POJO;

import java.util.List;

public class GitLabPushEvent {
    private String event_name;
    private String ref;
    private Project project;
    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    private List<Commit> commits;
    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public static class Project {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        }

    public static class Commit {
        private String id;
        private String message;
        private String timestamp;
        private String url;
        private Author author;
        

        public String getId() {
            return id;
        }


        public void setId(String id) {
            this.id = id;
        }


        public String getMessage() {
            return message;
        }


        public void setMessage(String message) {
            this.message = message;
        }


        public String getTimestamp() {
            return timestamp;
        }


        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }


        public String getUrl() {
            return url;
        }


        public void setUrl(String url) {
            this.url = url;
        }


        public Author getAuthor() {
            return author;
        }


        public void setAuthor(Author author) {
            this.author = author;
        }


        public static class Author {
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
            
        }
    }
}
