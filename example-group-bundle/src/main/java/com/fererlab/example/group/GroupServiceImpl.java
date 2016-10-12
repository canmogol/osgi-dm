package com.fererlab.example.group;

import com.fererlab.example.group.api.GroupService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GroupServiceImpl implements GroupService {

    @Override
    public List<String> getAllGroups() {
        return Arrays.asList("Admin", "Moderator", "User", "Guest");
    }

    @Override
    public List<String> getGroupsOfUser(String username) {
        if ("john".equals(username)) {
            return Arrays.asList("Moderator", "User");
        } else {
            return Collections.singletonList("Guest");
        }
    }

}
