package com.vmloft.develop.app.videoplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by chalilayang on 2017/11/27.
 */

public class VideoDetailBean implements Parcelable {


    /**
     * id : 26
     * code : RlLUWl
     * title : 和Martinez一起练背
     * intro : 和Martinez一起练背
     * pic : 64
     * thumb_pic : 65
     * file : 75
     * tag_ids :
     * length : 12:20
     * is_cnword : 0
     * is_commend : 0
     * collects : 0
     * likes : 0
     * shares : 0
     * caches : 0
     * play : 3
     * comments : 0
     * add_time : 1513092758
     * update_time : 1513094608
     * lastplay_time : 1513173328
     * channel_id : 1
     * channel_name : 频道12
     * channel_pic : 22
     * channel_update_time : 1512302789
     * channel_intro : 频道1的简介1212
     * pic_url : http://120.77.176.101/jianshen/Uploads/Picture/2017-12-13/5a2ffdced869a.jpg
     * thumb_pic_url : http://120.77.176.101/jianshen/Uploads/Picture/2017-12-13
     * /5a2ffdced869a_thumb.jpg
     * file_url : http://videos.baoge.tv/Uploads/Download/2017-12-12/5a2ff6899cce5.mp4
     * file_size : 1212121
     * channel_pic_url : http://120.77.176.101/jianshen/Uploads/Picture/2017-12-02/5a220e923f440.jpg
     * is_collect : 0
     * tags : []
     */

    private String id;
    private String code;
    private String title;
    private String intro;
    private String pic;
    private String thumb_pic;
    private String file;
    private String tag_ids;
    private String length;
    private String is_cnword;
    private String is_commend;
    private String collects;
    private String likes;
    private String shares;
    private String caches;
    private String play;
    private String comments;
    private String add_time;
    private String update_time;
    private String lastplay_time;
    private String channel_id;
    private String channel_name;
    private String channel_pic;
    private String channel_update_time;
    private String channel_intro;
    private String pic_url;
    private String thumb_pic_url;
    private String file_url;
    private String file_size;
    private String channel_pic_url;
    private String is_collect;
    private List<TagsBean> tags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getThumb_pic() {
        return thumb_pic;
    }

    public void setThumb_pic(String thumb_pic) {
        this.thumb_pic = thumb_pic;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getTag_ids() {
        return tag_ids;
    }

    public void setTag_ids(String tag_ids) {
        this.tag_ids = tag_ids;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getIs_cnword() {
        return is_cnword;
    }

    public void setIs_cnword(String is_cnword) {
        this.is_cnword = is_cnword;
    }

    public String getIs_commend() {
        return is_commend;
    }

    public void setIs_commend(String is_commend) {
        this.is_commend = is_commend;
    }

    public String getCollects() {
        return collects;
    }

    public void setCollects(String collects) {
        this.collects = collects;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public String getCaches() {
        return caches;
    }

    public void setCaches(String caches) {
        this.caches = caches;
    }

    public String getPlay() {
        return play;
    }

    public void setPlay(String play) {
        this.play = play;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getLastplay_time() {
        return lastplay_time;
    }

    public void setLastplay_time(String lastplay_time) {
        this.lastplay_time = lastplay_time;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getChannel_pic() {
        return channel_pic;
    }

    public void setChannel_pic(String channel_pic) {
        this.channel_pic = channel_pic;
    }

    public String getChannel_update_time() {
        return channel_update_time;
    }

    public void setChannel_update_time(String channel_update_time) {
        this.channel_update_time = channel_update_time;
    }

    public String getChannel_intro() {
        return channel_intro;
    }

    public void setChannel_intro(String channel_intro) {
        this.channel_intro = channel_intro;
    }

    public String getPic_url() {
        return pic_url;
    }

    public void setPic_url(String pic_url) {
        this.pic_url = pic_url;
    }

    public String getThumb_pic_url() {
        return thumb_pic_url;
    }

    public void setThumb_pic_url(String thumb_pic_url) {
        this.thumb_pic_url = thumb_pic_url;
    }

    public String getFile_url() {
        return file_url;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getChannel_pic_url() {
        return channel_pic_url;
    }

    public void setChannel_pic_url(String channel_pic_url) {
        this.channel_pic_url = channel_pic_url;
    }

    public String getIs_collect() {
        return is_collect;
    }

    public void setIs_collect(String is_collect) {
        this.is_collect = is_collect;
    }

    public List<TagsBean> getTags() {
        return tags;
    }

    public void setTags(List<TagsBean> tags) {
        this.tags = tags;
    }


    public static class TagsBean implements Parcelable {
        /**
         * id : 3
         * name : 标签356
         */

        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.name);
        }

        public TagsBean() {
        }

        protected TagsBean(Parcel in) {
            this.id = in.readString();
            this.name = in.readString();
        }

        public static final Creator<TagsBean> CREATOR = new Creator<TagsBean>() {
            @Override
            public TagsBean createFromParcel(Parcel source) {
                return new TagsBean(source);
            }

            @Override
            public TagsBean[] newArray(int size) {
                return new TagsBean[size];
            }
        };
    }

    public VideoDetailBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.code);
        dest.writeString(this.title);
        dest.writeString(this.intro);
        dest.writeString(this.pic);
        dest.writeString(this.thumb_pic);
        dest.writeString(this.file);
        dest.writeString(this.tag_ids);
        dest.writeString(this.length);
        dest.writeString(this.is_cnword);
        dest.writeString(this.is_commend);
        dest.writeString(this.collects);
        dest.writeString(this.likes);
        dest.writeString(this.shares);
        dest.writeString(this.caches);
        dest.writeString(this.play);
        dest.writeString(this.comments);
        dest.writeString(this.add_time);
        dest.writeString(this.update_time);
        dest.writeString(this.lastplay_time);
        dest.writeString(this.channel_id);
        dest.writeString(this.channel_name);
        dest.writeString(this.channel_pic);
        dest.writeString(this.channel_update_time);
        dest.writeString(this.channel_intro);
        dest.writeString(this.pic_url);
        dest.writeString(this.thumb_pic_url);
        dest.writeString(this.file_url);
        dest.writeString(this.file_size);
        dest.writeString(this.channel_pic_url);
        dest.writeString(this.is_collect);
        dest.writeTypedList(this.tags);
    }

    protected VideoDetailBean(Parcel in) {
        this.id = in.readString();
        this.code = in.readString();
        this.title = in.readString();
        this.intro = in.readString();
        this.pic = in.readString();
        this.thumb_pic = in.readString();
        this.file = in.readString();
        this.tag_ids = in.readString();
        this.length = in.readString();
        this.is_cnword = in.readString();
        this.is_commend = in.readString();
        this.collects = in.readString();
        this.likes = in.readString();
        this.shares = in.readString();
        this.caches = in.readString();
        this.play = in.readString();
        this.comments = in.readString();
        this.add_time = in.readString();
        this.update_time = in.readString();
        this.lastplay_time = in.readString();
        this.channel_id = in.readString();
        this.channel_name = in.readString();
        this.channel_pic = in.readString();
        this.channel_update_time = in.readString();
        this.channel_intro = in.readString();
        this.pic_url = in.readString();
        this.thumb_pic_url = in.readString();
        this.file_url = in.readString();
        this.file_size = in.readString();
        this.channel_pic_url = in.readString();
        this.is_collect = in.readString();
        this.tags = in.createTypedArrayList(TagsBean.CREATOR);
    }

    public static final Creator<VideoDetailBean> CREATOR = new Creator<VideoDetailBean>() {
        @Override
        public VideoDetailBean createFromParcel(Parcel source) {
            return new VideoDetailBean(source);
        }

        @Override
        public VideoDetailBean[] newArray(int size) {
            return new VideoDetailBean[size];
        }
    };
}
