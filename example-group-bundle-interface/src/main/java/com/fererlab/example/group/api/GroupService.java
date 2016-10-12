package com.fererlab.example.group.api;

import java.util.List;

public interface GroupService {

    List<String> getAllGroups();

    List<String> getGroupsOfUser(String username);

}
