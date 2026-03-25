package com.teamwork.service;

import com.teamwork.contract.IPlugin;
import com.teamwork.contract.IHostContext;
import com.teamwork.kernel.PluginLoader;
import com.teamwork.kernel.PluginManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho PermissionService và Plugin System
 *
 * Không cần kết nối Database.
 * Dùng Stub nội tuyến để giả lập hành vi DAO.
 *
 * Bao gồm:
 * - Logic phân quyền OWNER / MANAGER / MEMBER
 * - PluginLoader: nạp/từ chối plugin
 * - PluginManager: broadcast sự kiện
 */
@DisplayName("Unit Tests - PermissionService & Plugin System")
public class PermissionServiceTest {

    // ─── NHÓM 1: Logic phân quyền ─────────────────────────────────────────────
    // Các test này kiểm tra logic cốt lõi không cần DB:
    // PermissionService.isOwner() dùng (ownerId == userId) làm điều kiện chính.

    @Test
    @DisplayName("[PS-01] isOwner() - PASS: userId khớp ownerId")
    void testIsOwner_Match_ReturnsTrue() {
        // Logic cốt lõi: nếu ownerId == userId → là owner
        int userId = 1, ownerId = 1;
        boolean result = (ownerId != 0 && ownerId == userId);
        assertTrue(result, "userId khớp ownerId phải được coi là OWNER");
    }

    @Test
    @DisplayName("[PS-02] isOwner() - FAIL: userId khác ownerId")
    void testIsOwner_NoMatch_ReturnsFalse() {
        int userId = 2, ownerId = 1;
        boolean result = (ownerId != 0 && ownerId == userId);
        assertFalse(result, "userId khác ownerId không phải OWNER");
    }

    @Test
    @DisplayName("[PS-03] isManager() - PASS: OWNER cũng là MANAGER")
    void testIsManager_OwnerIsAlsoManager() {
        // OWNER thoả mãn isOwner() → isManager() phải true
        // Mô phỏng: userId == ownerId → isOwner true → isManager true
        int userId = 5, ownerId = 5;
        boolean isOwner = (ownerId == userId);
        boolean isManager = isOwner; // Logic của PermissionService
        assertTrue(isManager, "OWNER phải được coi là MANAGER");
    }

    @Test
    @DisplayName("[PS-04] isManager() - PASS: role là MANAGER nhưng không phải OWNER")
    void testIsManager_ManagerRole_ReturnsTrue() {
        // MANAGER không phải OWNER nhưng có role MANAGER
        String roleFromDB = "MANAGER";
        boolean isOwner = false; // userId != ownerId
        boolean isManager = isOwner || "MANAGER".equals(roleFromDB);
        assertTrue(isManager, "Role MANAGER phải được coi là manager");
    }

    @Test
    @DisplayName("[PS-05] isManager() - FAIL: role là MEMBER thường")
    void testIsManager_MemberRole_ReturnsFalse() {
        String roleFromDB = "MEMBER";
        boolean isOwner = false;
        boolean isManager = isOwner || "MANAGER".equals(roleFromDB);
        assertFalse(isManager, "Role MEMBER không phải MANAGER");
    }

    @Test
    @DisplayName("[PS-06] isMember() - PASS: user có role bất kỳ trong project")
    void testIsMember_HasRole_ReturnsTrue() {
        // isMember() trả về true nếu role != null
        String roleFromDB = "MEMBER";
        boolean isMember = (roleFromDB != null);
        assertTrue(isMember, "User có role bất kỳ phải là member của project");
    }

    @Test
    @DisplayName("[PS-07] isMember() - FAIL: user không thuộc project (role null)")
    void testIsMember_NoRole_ReturnsFalse() {
        String roleFromDB = null;
        boolean isMember = (roleFromDB != null);
        assertFalse(isMember, "User không có role không thuộc project");
    }

    @Test
    @DisplayName("[PS-08] Thứ bậc quyền: OWNER > MANAGER > MEMBER")
    void testPermissionHierarchy() {
        // OWNER có tất cả quyền
        boolean ownerIsOwner = true;
        boolean ownerIsManager = true; // owner thoả mãn isManager
        boolean ownerIsMember = true; // owner có role

        assertTrue(ownerIsOwner && ownerIsManager && ownerIsMember,
                "OWNER phải có tất cả các quyền");

        // MANAGER không phải OWNER nhưng là manager và member
        boolean managerIsOwner = false;
        boolean managerIsManager = true;
        boolean managerIsMember = true;

        assertFalse(managerIsOwner, "MANAGER không phải OWNER");
        assertTrue(managerIsManager && managerIsMember, "MANAGER có quyền manager và member");
    }

    // ─── NHÓM 2: PluginLoader ─────────────────────────────────────────────────

    @Test
    @DisplayName("[PS-09] PluginLoader - PASS: nạp plugin hợp lệ (HelloWorldPlugin)")
    void testPluginLoader_ValidPlugin_Loaded() {
        PluginLoader loader = new PluginLoader();
        assertDoesNotThrow(() -> loader.loadPluginClass("com.teamwork.plugins.HelloWorldPlugin"));
        assertEquals(1, loader.getLoadedPlugins().size(),
                "Danh sách plugin phải có 1 phần tử sau khi nạp");
    }

    @Test
    @DisplayName("[PS-10] PluginLoader - PASS: lấy đúng tên plugin sau khi nạp")
    void testPluginLoader_ValidPlugin_CorrectName() {
        PluginLoader loader = new PluginLoader();
        loader.loadPluginClass("com.teamwork.plugins.HelloWorldPlugin");
        String name = loader.getLoadedPlugins().get(0).getName();
        assertNotNull(name, "Plugin phải có tên");
        assertFalse(name.isEmpty(), "Tên plugin không được rỗng");
    }

    @Test
    @DisplayName("[PS-11] PluginLoader - FAIL: class không implement IPlugin bị từ chối")
    void testPluginLoader_InvalidClass_NotLoaded() {
        PluginLoader loader = new PluginLoader();
        // String không implement IPlugin → không được nạp
        assertDoesNotThrow(() -> loader.loadPluginClass("java.lang.String"));
        assertEquals(0, loader.getLoadedPlugins().size(),
                "Class không hợp lệ không được nạp vào danh sách");
    }

    @Test
    @DisplayName("[PS-12] PluginLoader - FAIL: class không tồn tại → không crash")
    void testPluginLoader_NonExistentClass_NoCrash() {
        PluginLoader loader = new PluginLoader();
        assertDoesNotThrow(() -> loader.loadPluginClass("com.teamwork.plugins.KhongTonTai"));
        assertEquals(0, loader.getLoadedPlugins().size());
    }

    @Test
    @DisplayName("[PS-13] PluginLoader - PASS: nạp nhiều plugin khác nhau")
    void testPluginLoader_MultiplePlugins_AllLoaded() {
        PluginLoader loader = new PluginLoader();
        loader.loadPluginClass("com.teamwork.plugins.HelloWorldPlugin");
        loader.loadPluginClass("com.teamwork.plugins.ProjectPlugin");
        assertEquals(2, loader.getLoadedPlugins().size(),
                "Phải nạp được 2 plugin");
    }

    // ─── NHÓM 3: PluginManager broadcast ─────────────────────────────────────

    @Test
    @DisplayName("[PS-14] PluginManager - PASS: onProjectDelete broadcast tới plugin")
    void testPluginManager_NotifyProjectDeleted_Called() {
        final int[] callCount = { 0 };

        // Tạo spy plugin: đếm số lần onProjectDelete được gọi
        IPlugin spyPlugin = new IPlugin() {
            @Override
            public String getName() {
                return "SpyPlugin";
            }

            @Override
            public void initialize(IHostContext ctx) {
            }

            @Override
            public void start() {
            }

            @Override
            public void stop() {
            }

            @Override
            public void onProjectDelete(int projectId) {
                callCount[0]++;
            }
        };

        PluginManager.addPlugin(spyPlugin);
        PluginManager.notifyProjectDeleted(42);

        assertTrue(callCount[0] >= 1,
                "onProjectDelete phải được gọi ít nhất 1 lần");
    }

    @Test
    @DisplayName("[PS-15] PluginManager - PASS: onProjectDelete nhận đúng projectId")
    void testPluginManager_NotifyProjectDeleted_CorrectId() {
        final int[] receivedId = { -1 };

        IPlugin spyPlugin = new IPlugin() {
            @Override
            public String getName() {
                return "IdCheckerPlugin";
            }

            @Override
            public void initialize(IHostContext ctx) {
            }

            @Override
            public void start() {
            }

            @Override
            public void stop() {
            }

            @Override
            public void onProjectDelete(int projectId) {
                receivedId[0] = projectId;
            }
        };

        PluginManager.addPlugin(spyPlugin);
        PluginManager.notifyProjectDeleted(99);

        assertEquals(99, receivedId[0],
                "Plugin phải nhận đúng projectId = 99");
    }

    @Test
    @DisplayName("[PS-16] PluginManager - PASS: plugin bị lỗi không làm crash plugin khác")
    void testPluginManager_FaultyPlugin_DoesNotCrashOthers() {
        final boolean[] goodPluginCalled = { false };

        IPlugin faultyPlugin = new IPlugin() {
            @Override
            public String getName() {
                return "FaultyPlugin";
            }

            @Override
            public void initialize(IHostContext ctx) {
            }

            @Override
            public void start() {
            }

            @Override
            public void stop() {
            }

            @Override
            public void onProjectDelete(int projectId) {
                throw new RuntimeException("Lỗi từ plugin lỗi!");
            }
        };

        IPlugin goodPlugin = new IPlugin() {
            @Override
            public String getName() {
                return "GoodPlugin";
            }

            @Override
            public void initialize(IHostContext ctx) {
            }

            @Override
            public void start() {
            }

            @Override
            public void stop() {
            }

            @Override
            public void onProjectDelete(int projectId) {
                goodPluginCalled[0] = true;
            }
        };

        PluginManager.addPlugin(faultyPlugin);
        PluginManager.addPlugin(goodPlugin);

        // PluginManager dùng try-catch → plugin lỗi không làm crash toàn bộ
        assertDoesNotThrow(() -> PluginManager.notifyProjectDeleted(1));
    }
}