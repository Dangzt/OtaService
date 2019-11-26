package com.example.upgrading.bean;

import java.util.List;

public class UpdataBean {

    /**
     * code : 200
     * message : success
     * data : {"deviceName":"SAI_MINIPODPLUS","deviceType":"SAI_MINIPODPLUS","version":"V1.0.2","forceVersion":"V1.0.1","incrementalUpgrade":[{"supportVersion":"V1.0.1","url":"https://ota.soundai.cn/yg/minipodplus/software.swu","md5sum":"032f9dadffc6072232739f36f83c55a5"},{"supportVersion":"V0.0.1","url":"https://ota.soundai.cn/yg/minipodplus/software.swu","md5sum":"032f9dadffc6072232739f36f83c55a5"}],"fullUpgrade":{"url":"https://ota.soundai.cn/yg/minipodplus/software.swu","md5sum":"032f9dadffc6072232739f36f83c55a5"},"brief":"简介"}
     */

    private int code;
    private String message;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * deviceName : SAI_MINIPODPLUS
         * deviceType : SAI_MINIPODPLUS
         * version : V1.0.2
         * forceVersion : V1.0.1
         * incrementalUpgrade : [{"supportVersion":"V1.0.1","url":"https://ota.soundai.cn/yg/minipodplus/software.swu","md5sum":"032f9dadffc6072232739f36f83c55a5"},{"supportVersion":"V0.0.1","url":"https://ota.soundai.cn/yg/minipodplus/software.swu","md5sum":"032f9dadffc6072232739f36f83c55a5"}]
         * fullUpgrade : {"url":"https://ota.soundai.cn/yg/minipodplus/software.swu","md5sum":"032f9dadffc6072232739f36f83c55a5"}
         * brief : 简介
         */

        private String deviceName;
        private String deviceType;
        private String version;
        private String forceVersion;
        private FullUpgradeBean fullUpgrade;
        private String brief;
        private List<IncrementalUpgradeBean> incrementalUpgrade;

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getForceVersion() {
            return forceVersion;
        }

        public void setForceVersion(String forceVersion) {
            this.forceVersion = forceVersion;
        }

        public FullUpgradeBean getFullUpgrade() {
            return fullUpgrade;
        }

        public void setFullUpgrade(FullUpgradeBean fullUpgrade) {
            this.fullUpgrade = fullUpgrade;
        }

        public String getBrief() {
            return brief;
        }

        public void setBrief(String brief) {
            this.brief = brief;
        }

        public List<IncrementalUpgradeBean> getIncrementalUpgrade() {
            return incrementalUpgrade;
        }

        public void setIncrementalUpgrade(List<IncrementalUpgradeBean> incrementalUpgrade) {
            this.incrementalUpgrade = incrementalUpgrade;
        }

        public static class FullUpgradeBean {
            /**
             * url : https://ota.soundai.cn/yg/minipodplus/software.swu
             * md5sum : 032f9dadffc6072232739f36f83c55a5
             */

            private String url;
            private String md5sum;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getMd5sum() {
                return md5sum;
            }

            public void setMd5sum(String md5sum) {
                this.md5sum = md5sum;
            }
        }

        public static class IncrementalUpgradeBean {
            /**
             * supportVersion : V1.0.1
             * url : https://ota.soundai.cn/yg/minipodplus/software.swu
             * md5sum : 032f9dadffc6072232739f36f83c55a5
             */

            private String supportVersion;
            private String url;
            private String md5sum;

            public String getSupportVersion() {
                return supportVersion;
            }

            public void setSupportVersion(String supportVersion) {
                this.supportVersion = supportVersion;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getMd5sum() {
                return md5sum;
            }

            public void setMd5sum(String md5sum) {
                this.md5sum = md5sum;
            }
        }
    }
}
