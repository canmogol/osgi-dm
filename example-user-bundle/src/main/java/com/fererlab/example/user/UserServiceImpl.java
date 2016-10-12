package com.fererlab.example.user;

import com.fererlab.example.user.api.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserServiceImpl implements UserService {

    @Override
    public List<String> getAllUsers() {
        return Arrays.asList("john", "jim");
    }

    @Override
    public List<String> getAllUsersOfGroup(String groupname) {
        if ("Admin".equals(groupname)) {
            return Collections.singletonList("john");
        } else {
            return Arrays.asList("john", "jim");
        }
    }

}
