package com.cd.statussaver.interfaces;


import com.cd.statussaver.model.FBStoryModel.NodeModel;
import com.cd.statussaver.model.story.TrayModel;

public interface UserListInterface {
    void userListClick(int position, TrayModel trayModel);
    void fbUserListClick(int position, NodeModel trayModel);
}
