// frontend/src/api/taskApi.js
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/tasks';

export const taskApi = {
    // Hàm gọi API lấy danh sách (GET)
    getAllTasks: async () => {
        try {
            const response = await axios.get(API_URL);
            return response.data;
        } catch (error) {
            console.error("Lỗi khi lấy danh sách Task:", error);
            throw error;
        }
    },

    // Hàm gọi API tạo công việc mới (POST)
    createTask: async (taskData) => {
        try {
            const response = await axios.post(API_URL, taskData);
            return response.data;
        } catch (error) {
            console.error("Lỗi khi tạo Task mới:", error);
            throw error;
        }
    }
};