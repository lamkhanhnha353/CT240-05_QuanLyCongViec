<template>
  <button @click="generateExcel" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-bold shadow-md shadow-emerald-600/30 transition-all flex items-center hover:-translate-y-0.5">
    <span class="mr-2 text-lg leading-none">📥</span> Tải Báo Cáo (.xlsx)
  </button>
</template>

<script setup>
import * as XLSX from 'xlsx-js-style';

const props = defineProps({
  projectName: { type: String, required: true },
  tasks: { type: Array, required: true },
  members: { type: Array, required: true }
});

const formatStatus = (s) => s==='TODO'?'Cần làm':s==='IN_PROGRESS'?'Đang làm':s==='DONE'?'Hoàn thành':'Đã hủy';
const formatPriority = (p) => p === 'HIGH' ? 'Cao' : p === 'MEDIUM' ? 'Trung bình' : 'Thấp';
const formatDateFull = (d) => (!d || d === 'null') ? '---' : d.split('-').length === 3 ? `${d.split('-')[2]}/${d.split('-')[1]}/${d.split('-')[0]}` : d;

const getAssigneeNames = (idsStr) => {
  if (!idsStr || idsStr.trim() === '') return 'Chưa phân công';
  return idsStr.split(',').map(id => {
    const m = props.members.find(x => x.id == id);
    return m ? m.fullName : 'Unknown';
  }).join(', ');
};

const generateExcel = () => {
  if (!props.tasks || props.tasks.length === 0) {
    alert("Dự án hiện chưa có công việc nào để xuất báo cáo!");
    return;
  }

  const today = new Date();
  today.setHours(0,0,0,0);

  // ==========================================
  // 1. TẠO DATA CHO SHEET THỐNG KÊ
  // ==========================================
  let todo = 0, inProgress = 0, done = 0, cancel = 0;
  let overdue = 0, highPriority = 0;
  const workload = {};

  props.members.forEach(m => workload[m.fullName] = 0);
  workload['Chưa phân công'] = 0;

  props.tasks.forEach(t => {
    if (t.status === 'TODO') todo++;
    else if (t.status === 'IN_PROGRESS') inProgress++;
    else if (t.status === 'DONE') done++;
    else if (t.status === 'CANCEL') cancel++;

    if (t.priority === 'HIGH' && t.status !== 'DONE' && t.status !== 'CANCEL') highPriority++;

    if (t.status !== 'DONE' && t.status !== 'CANCEL' && t.deadline && t.deadline !== 'null') {
      const taskDate = new Date(t.deadline); taskDate.setHours(0,0,0,0);
      if (taskDate < today) overdue++;
    }

    if (t.status === 'TODO' || t.status === 'IN_PROGRESS') {
      if (!t.assigneeIds || t.assigneeIds.trim() === '') workload['Chưa phân công']++;
      else t.assigneeIds.split(',').forEach(id => { const m = props.members.find(x => x.id == id); if (m) workload[m.fullName]++; });
    }
  });

  const totalActive = props.tasks.length - cancel;
  const rate = totalActive > 0 ? Math.round((done / totalActive) * 100) : 0;

  const summaryData = [
    { "CHỈ SỐ BÁO CÁO": "Tên Dự Án", "KẾT QUẢ": props.projectName },
    { "CHỈ SỐ BÁO CÁO": "Ngày Xuất Báo Cáo", "KẾT QUẢ": new Date().toLocaleDateString('vi-VN') },
    { "CHỈ SỐ BÁO CÁO": "----------------", "KẾT QUẢ": "----------------" },
    { "CHỈ SỐ BÁO CÁO": "Tổng số công việc", "KẾT QUẢ": props.tasks.length },
    { "CHỈ SỐ BÁO CÁO": "Tỷ lệ hoàn thành", "KẾT QUẢ": `${rate}%` },
    { "CHỈ SỐ BÁO CÁO": "Số việc Cần Làm", "KẾT QUẢ": todo },
    { "CHỈ SỐ BÁO CÁO": "Số việc Đang Làm", "KẾT QUẢ": inProgress },
    { "CHỈ SỐ BÁO CÁO": "Số việc Đã Hoàn Thành", "KẾT QUẢ": done },
    { "CHỈ SỐ BÁO CÁO": "CẢNH BÁO: Số việc Trễ Hạn", "KẾT QUẢ": overdue },
    { "CHỈ SỐ BÁO CÁO": "CẢNH BÁO: Việc Khẩn Cấp (Cao)", "KẾT QUẢ": highPriority },
    { "CHỈ SỐ BÁO CÁO": "----------------", "KẾT QUẢ": "----------------" },
    { "CHỈ SỐ BÁO CÁO": "THỐNG KÊ KHỐI LƯỢNG CÔNG VIỆC", "KẾT QUẢ": "(Chỉ tính việc chưa hoàn thành)" }
  ];

  Object.keys(workload).forEach(name => {
    if (workload[name] > 0 || name === 'Chưa phân công') {
      summaryData.push({ "CHỈ SỐ BÁO CÁO": ` - ${name}`, "KẾT QUẢ": `${workload[name]} việc` });
    }
  });

  // ==========================================
  // 2. TẠO DATA CHO SHEET CHI TIẾT
  // ==========================================
  const detailsData = props.tasks.map((t, index) => {
    return {
      "STT": index + 1,
      "Tên Công Việc": t.title,
      "Trạng Thái": formatStatus(t.status),
      "Mức Độ": formatPriority(t.priority),
      "Người Phụ Trách": getAssigneeNames(t.assigneeIds),
      "Bắt Đầu": formatDateFull(t.startDate),
      "Hạn Chót": formatDateFull(t.deadline),
      "Nhãn": t.tags || '---',
      "Mô Tả": t.description || '---'
    };
  });

  const wb = XLSX.utils.book_new();

  // ==========================================
  //  HÀM STYLE (THIẾT KẾ LẠI GIAO DIỆN SÁNG SỦA) 
  // ==========================================
  const defaultBorderStyle = {
    top: { style: "thin", color: { rgb: "E2E8F0" } }, // Xám rất nhạt
    bottom: { style: "thin", color: { rgb: "E2E8F0" } },
    left: { style: "thin", color: { rgb: "E2E8F0" } },
    right: { style: "thin", color: { rgb: "E2E8F0" } }
  };

  const headerStyle = {
    fill: { fgColor: { rgb: "3B82F6" } }, // Xanh dương tươi sáng (Màu Blue-500)
    font: { color: { rgb: "FFFFFF" }, bold: true, name: "Arial", sz: 11 },
    border: defaultBorderStyle,
    alignment: { horizontal: "center", vertical: "center" }
  };

  // --- STYLE SHEET 1 (THỐNG KÊ) ---
  const wsSummary = XLSX.utils.json_to_sheet(summaryData);
  wsSummary['!cols'] = [{ wch: 40 }, { wch: 30 }];
  
  const range1 = XLSX.utils.decode_range(wsSummary['!ref']);
  for(let R = range1.s.r; R <= range1.e.r; ++R) {
    for(let C = range1.s.c; C <= range1.e.c; ++C) {
      const cellAddress = XLSX.utils.encode_cell({r: R, c: C});
      if (!wsSummary[cellAddress]) continue;
      
      if (R === 0) {
        wsSummary[cellAddress].s = headerStyle;
      } else {
        wsSummary[cellAddress].s = { 
          font: { name: "Arial", sz: 11, bold: (C === 0) },
          border: defaultBorderStyle,
          alignment: { vertical: "center" } 
        };
      }
    }
  }
  XLSX.utils.book_append_sheet(wb, wsSummary, "1. Thống Kê Tổng Quan");

  // --- STYLE SHEET 2 (CHI TIẾT) ---
  const wsDetails = XLSX.utils.json_to_sheet(detailsData);
  wsDetails['!cols'] = [
    { wch: 5 }, { wch: 45 }, { wch: 15 }, { wch: 15 }, { wch: 30 }, 
    { wch: 15 }, { wch: 15 }, { wch: 25 }, { wch: 50 }
  ];

  const range2 = XLSX.utils.decode_range(wsDetails['!ref']);
  for(let R = range2.s.r; R <= range2.e.r; ++R) {
    for(let C = range2.s.c; C <= range2.e.c; ++C) {
      const cellAddress = XLSX.utils.encode_cell({r: R, c: C});
      if (!wsDetails[cellAddress]) continue;

      if (R === 0) {
        wsDetails[cellAddress].s = headerStyle;
      } else {
        wsDetails[cellAddress].s = { 
          font: { name: "Arial", sz: 11 }, 
          border: defaultBorderStyle, 
          alignment: { vertical: "center" } 
        };
        
        const val = wsDetails[cellAddress].v;
        
        //  TÔ MÀU KIỂU PASTEL CHO TRẠNG THÁI (Nền nhạt, chữ đậm)
        if (C === 2) {
           wsDetails[cellAddress].s.font.bold = true;
           if(val === 'Hoàn thành') {
               wsDetails[cellAddress].s.font.color = { rgb: "065F46" }; // Xanh lá đậm
               wsDetails[cellAddress].s.fill = { fgColor: { rgb: "D1FAE5" } }; // Nền xanh lá nhạt
           }
           if(val === 'Đang làm') {
               wsDetails[cellAddress].s.font.color = { rgb: "1E3A8A" }; // Xanh dương đậm
               wsDetails[cellAddress].s.fill = { fgColor: { rgb: "DBEAFE" } }; // Nền xanh dương nhạt
           }
           if(val === 'Đã hủy') {
               wsDetails[cellAddress].s.font.color = { rgb: "991B1B" }; // Đỏ đậm
               wsDetails[cellAddress].s.fill = { fgColor: { rgb: "FEE2E2" } }; // Nền đỏ nhạt
           }
           if(val === 'Cần làm') {
               wsDetails[cellAddress].s.font.color = { rgb: "334155" }; // Xám đậm
               wsDetails[cellAddress].s.fill = { fgColor: { rgb: "F1F5F9" } }; // Nền xám nhạt
           }
           wsDetails[cellAddress].s.alignment.horizontal = "center";
        }
        
        //  TÔ MÀU KIỂU PASTEL CHO MỨC ĐỘ
        if (C === 3) {
           wsDetails[cellAddress].s.font.bold = true;
           if(val === 'Cao') {
               wsDetails[cellAddress].s.font.color = { rgb: "991B1B" }; // Đỏ đậm
               wsDetails[cellAddress].s.fill = { fgColor: { rgb: "FEE2E2" } }; // Nền đỏ nhạt
           }
           if(val === 'Trung bình') {
               wsDetails[cellAddress].s.font.color = { rgb: "92400E" }; // Cam đậm
               wsDetails[cellAddress].s.fill = { fgColor: { rgb: "FEF3C7" } }; // Nền vàng nhạt
           }
           if(val === 'Thấp') {
               wsDetails[cellAddress].s.font.color = { rgb: "334155" }; // Xám đậm
               wsDetails[cellAddress].s.fill = { fgColor: { rgb: "F1F5F9" } }; // Nền xám nhạt
           }
           wsDetails[cellAddress].s.alignment.horizontal = "center";
        }

        // Canh giữa STT và Ngày tháng
        if (C === 0 || C === 5 || C === 6) {
          wsDetails[cellAddress].s.alignment.horizontal = "center";
        }
      }
    }
  }
  XLSX.utils.book_append_sheet(wb, wsDetails, "2. Chi Tiết Công Việc");

  // ==========================================
  // 3. TẢI FILE XUỐNG
  // ==========================================
  const dateStr = new Date().toISOString().slice(0,10).replace(/-/g, "");
  XLSX.writeFile(wb, `BaoCao_DuAn_${props.projectName.replace(/\s+/g, '_')}_${dateStr}.xlsx`);
};
</script>