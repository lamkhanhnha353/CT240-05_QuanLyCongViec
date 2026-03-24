<template>
  <div class="flex flex-col w-full h-full relative min-h-0">
    
    <div class="flex px-6 py-4 shrink-0 justify-between items-center gap-4 border-b border-slate-100 dark:border-slate-800">
      
      <div class="relative flex-1 w-full max-w-md">
        <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">🔍</span>
        <input v-model="searchQuery" type="text" placeholder="Tìm tên công việc..." class="w-full pl-10 pr-10 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm font-medium outline-none focus:border-blue-500 shadow-sm dark:text-white transition-all" />
        
        <button @click="startVoiceSearch" class="absolute inset-y-0 right-0 pr-3 flex items-center transition-colors focus:outline-none" :class="isListening ? 'text-red-500 animate-pulse' : 'text-slate-400 hover:text-blue-500'" title="Tìm kiếm bằng giọng nói">
          <svg v-if="!isListening" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"></path></svg>
          <span v-else class="text-xl">🎙️</span>
        </button>
      </div>

      <div class="flex items-center space-x-3 shrink-0">
        
        <button @click="openTaskModal('TODO')" class="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-bold rounded-xl shadow-md shadow-blue-500/30 transition-all flex items-center hover:-translate-y-0.5 hidden sm:flex">
          <span class="mr-2 text-lg leading-none">+</span> Thêm công việc
        </button>
        <button @click="openTaskModal('TODO')" class="p-2.5 bg-blue-600 text-white font-bold rounded-xl shadow-md sm:hidden flex items-center justify-center">
          <span class="text-lg leading-none">+</span>
        </button>

        <div class="bg-slate-200/70 dark:bg-slate-800/70 p-1.5 rounded-xl flex space-x-1 shadow-inner border border-slate-300/50 dark:border-slate-700/50 hidden md:flex">
          <button @click="viewMode = 'board'" :class="viewMode === 'board' ? 'bg-white dark:bg-slate-700 shadow-md text-blue-600 dark:text-blue-400' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-300/50 dark:hover:bg-slate-700/50'" class="p-2 rounded-lg transition-all" title="Dạng Kanban"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path></svg></button>
          <button @click="viewMode = 'table'" :class="viewMode === 'table' ? 'bg-white dark:bg-slate-700 shadow-md text-blue-600 dark:text-blue-400' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-300/50 dark:hover:bg-slate-700/50'" class="p-2 rounded-lg transition-all" title="Dạng Danh Sách"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg></button>
          <button @click="viewMode = 'calendar'" :class="viewMode === 'calendar' ? 'bg-white dark:bg-slate-700 shadow-md text-blue-600 dark:text-blue-400' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-300/50 dark:hover:bg-slate-700/50'" class="p-2 rounded-lg transition-all" title="Dạng Lịch (Calendar)"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg></button>
        </div>

        <button @click="showFilters = true" class="relative p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-600 hover:text-blue-600 dark:text-slate-300 dark:hover:text-blue-400 shadow-sm transition-all flex items-center hover:-translate-y-0.5" title="Lọc & Sắp xếp">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"></path></svg>
          <span class="hidden md:block ml-2 text-sm font-bold">Lọc / Sắp xếp</span>
          <span v-if="activeFilterCount > 0" class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-[10px] font-black rounded-full flex items-center justify-center shadow-sm border-2 border-white dark:border-slate-900">{{ activeFilterCount }}</span>
        </button>

      </div>
    </div>

    <div v-if="viewMode === 'board'" class="flex-1 min-h-0 overflow-x-auto overflow-y-hidden px-6 pb-6 pt-4 flex items-start gap-6 custom-scrollbar w-full bg-slate-50/30 dark:bg-[#0f172a]/30">
      <div v-for="column in kanbanColumns" :key="column.id" class="flex-1 min-w-[320px] max-w-[400px] flex flex-col h-full max-h-full">
        <div class="flex justify-between items-center mb-3 px-1 shrink-0">
          <h3 class="font-bold text-slate-700 dark:text-slate-200 flex items-center text-sm uppercase tracking-wider">
            <span class="w-3 h-3 rounded-full mr-2 shadow-sm" :class="column.colorClass"></span>
            {{ column.title }}
            <span class="ml-2 bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 text-xs px-2 py-0.5 rounded-full font-black">{{ boardTasks[column.id]?.filter(matchFilter).length || 0 }}</span>
          </h3>
        </div>

        <div class="bg-slate-200/50 dark:bg-[#1e293b]/50 rounded-2xl p-3 pb-4 flex-1 min-h-0 overflow-y-auto flex flex-col border border-slate-200/60 dark:border-slate-700/60 shadow-inner custom-scrollbar">
          <draggable v-model="boardTasks[column.id]" group="tasks" item-key="id" @change="onTaskMoved($event, column.id)" class="flex-1 min-h-[100px] space-y-3" ghost-class="opacity-40" drag-class="cursor-grabbing" :animation="200">
            <template #item="{ element }">
              <div v-show="matchFilter(element)" @click="openEditModal(element)" class="task-card bg-white dark:bg-slate-800 p-4 rounded-xl shadow-sm border-l-4 cursor-grab active:cursor-grabbing hover:shadow-md transition-all relative group" :class="getPriorityBorder(element.priority)">
                
                <div class="flex justify-between items-start mb-2">
                  <span class="text-[10px] font-black uppercase tracking-widest px-2 py-1 rounded-md shadow-sm" :class="getPriorityTag(element.priority)">{{ formatPriority(element.priority) }}</span>
                  <span v-if="getDeadlineBadge(element.deadline, element.status)" 
                        class="text-[9px] font-black tracking-wider px-2 py-1 rounded-md shadow-sm flex items-center" 
                        :class="getDeadlineBadge(element.deadline, element.status).class">
                    <span class="mr-1 text-xs">{{ getDeadlineBadge(element.deadline, element.status).icon }}</span>
                    {{ getDeadlineBadge(element.deadline, element.status).text }}
                  </span>
                </div>
                
                <h4 class="task-card-title font-bold text-slate-800 dark:text-white text-sm mb-1 leading-snug pr-2" :class="column.id === 'CANCEL' ? 'line-through opacity-70' : ''">{{ element.title }}</h4>
                
                <div v-if="element.tags" class="flex flex-wrap gap-1.5 mb-2 mt-1">
                  <span v-for="(tag, idx) in getTagsArray(element.tags)" :key="idx" class="px-2 py-0.5 rounded-md text-[9px] font-black tracking-wider uppercase shadow-sm" :class="getTagColor(tag)">{{ tag }}</span>
                </div>
                <p v-if="element.description" class="text-xs text-slate-500 dark:text-slate-400 line-clamp-2 mb-3">{{ element.description }}</p>
                <p v-else class="text-xs text-slate-400 dark:text-slate-500 italic mb-3">Không có mô tả</p>
                <div class="flex justify-between items-center border-t border-slate-50 dark:border-slate-700/50 pt-3 mt-2">
                  <div class="flex items-center space-x-1.5 text-xs font-bold" :class="isDeadlineNear(element.deadline) && column.id !== 'DONE' && column.id !== 'CANCEL' ? 'text-red-500 dark:text-red-400' : 'text-slate-500 dark:text-slate-400'">
                    <span>⏳</span> 
                    <span v-if="!element.startDate && (!element.deadline || element.deadline === 'null')">Chưa có hạn</span>
                    <span v-else>{{ formatDate(element.startDate) }} - {{ formatDate(element.deadline) }}</span>
                  </div>
                  <div class="flex -space-x-1 relative z-0">
                    <template v-if="getAssigneeArray(element.assigneeName).length > 0">
                      <div class="w-6 h-6 rounded-full bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-300 border-2 border-white dark:border-slate-800 flex items-center justify-center text-[9px] font-black shadow-sm" :title="getAssigneeArray(element.assigneeName)[0]">
                        {{ getAssigneeArray(element.assigneeName)[0].charAt(0).toUpperCase() }}
                      </div>
                      <div v-if="getAssigneeArray(element.assigneeName).length > 1" class="w-6 h-6 rounded-full bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 border-2 border-white dark:border-slate-800 flex items-center justify-center text-[9px] font-black shadow-sm" title="Nhiều người tham gia">
                        +{{ getAssigneeArray(element.assigneeName).length - 1 }}
                      </div>
                    </template>
                    <div v-else class="w-6 h-6 rounded-full bg-slate-100 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 border-dashed flex items-center justify-center text-slate-400 text-[10px]" title="Chưa phân công">?</div>
                  </div>
                </div>
              </div>
            </template>
          </draggable>
        </div>
      </div>
    </div>

    <div v-if="viewMode === 'table'" class="flex-1 min-h-0 overflow-auto px-6 pb-6 pt-4 custom-scrollbar w-full flex flex-col bg-slate-50/30 dark:bg-[#0f172a]/30">
      <div class="bg-white dark:bg-slate-800 rounded-3xl shadow-sm border border-slate-200 dark:border-slate-700 overflow-hidden flex flex-col flex-1 min-h-0">
        <div class="overflow-y-auto custom-scrollbar flex-1 min-h-0">
          <table class="w-full text-left border-collapse min-w-max">
            <thead class="sticky top-0 bg-slate-50 dark:bg-slate-800/90 backdrop-blur-md z-10 shadow-sm border-b border-slate-200 dark:border-slate-700">
              <tr class="text-[11px] uppercase tracking-widest text-slate-500 dark:text-slate-400">
                <th class="py-5 px-4 font-black text-center w-16">STT</th>
                <th class="py-5 px-4 font-black w-[40%]">Tên công việc & Nhãn</th>
                <th class="py-5 px-4 font-black">Trạng thái</th>
                <th class="py-5 px-4 font-black">Ưu tiên</th>
                <th class="py-5 px-4 font-black">Phân công</th>
                <th class="py-5 px-6 font-black text-right">Lộ trình (Timeline)</th>
              </tr>
            </thead>
            <tbody v-if="paginatedTasks.length > 0">
              <tr v-for="(task, index) in paginatedTasks" :key="task.id" @click="openEditModal(task)" class="border-b border-slate-200 dark:border-slate-700/70 hover:bg-blue-50/80 dark:hover:bg-slate-700/50 cursor-pointer transition-colors group even:bg-slate-50/50 dark:even:bg-slate-900/40">
                <td class="py-4 px-4 text-center font-bold text-slate-500 dark:text-slate-400">
                  {{ (currentPage - 1) * itemsPerPage + index + 1 }}
                </td>
                
                <td class="py-4 px-4">
                  <p class="font-bold text-slate-800 dark:text-white text-sm mb-1 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors" :class="task.status === 'CANCEL' ? 'line-through opacity-60' : ''">{{ task.title }}</p>
                  <div v-if="task.tags" class="flex flex-wrap gap-1.5 mt-1.5">
                    <span v-for="(tag, idx) in getTagsArray(task.tags)" :key="idx" class="px-2 py-0.5 rounded text-[9px] font-black uppercase shadow-sm" :class="getTagColor(tag)">{{ tag }}</span>
                  </div>
                </td>
                
                <td class="py-4 px-4"><span class="px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider shadow-sm" :class="getStatusStyle(task.status)">{{ formatStatus(task.status) }}</span></td>
                <td class="py-4 px-4"><span class="text-[10px] font-black uppercase tracking-widest px-2 py-1 rounded-md shadow-sm" :class="getPriorityTag(task.priority)">{{ formatPriority(task.priority) }}</span></td>
                
                <td class="py-4 px-4">
                  <div class="flex -space-x-1">
                    <template v-if="getAssigneeArray(task.assigneeName).length > 0">
                      <div class="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-300 border-2 border-white dark:border-slate-800 flex items-center justify-center text-xs font-black shadow-sm" :title="getAssigneeArray(task.assigneeName)[0]">{{ getAssigneeArray(task.assigneeName)[0].charAt(0).toUpperCase() }}</div>
                      <div v-if="getAssigneeArray(task.assigneeName).length > 1" class="w-8 h-8 rounded-full bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 border-2 border-white dark:border-slate-800 flex items-center justify-center text-xs font-black shadow-sm">+{{ getAssigneeArray(task.assigneeName).length - 1 }}</div>
                    </template>
                    <div v-else class="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 border-dashed flex items-center justify-center text-slate-400 text-xs">?</div>
                  </div>
                </td>
                
                <td class="py-4 px-6 text-right">
                  <div v-if="!task.startDate && (!task.deadline || task.deadline === 'null')" class="text-xs font-bold text-slate-400 bg-slate-100 dark:bg-slate-800 px-3 py-1.5 rounded-lg inline-block">Chưa có hạn</div>
                  <div v-else class="text-xs font-bold flex items-center justify-end space-x-2 text-slate-600 dark:text-slate-400">
                    <span class="bg-slate-100 dark:bg-slate-800 px-2 py-1.5 rounded-md shadow-sm">{{ formatDateFull(task.startDate) || '---' }}</span>
                    <span class="text-slate-300 dark:text-slate-600 font-normal">➔</span>
                    <span class="px-2 py-1.5 rounded-md shadow-sm" :class="isDeadlineNear(task.deadline) && task.status !== 'DONE' && task.status !== 'CANCEL' ? 'text-red-600 bg-red-100 dark:bg-red-900/40 dark:text-red-400' : 'bg-slate-100 dark:bg-slate-800'">{{ formatDateFull(task.deadline) || '---' }}</span>
                  </div>
                </td>
              </tr>
            </tbody>
            <tbody v-else>
              <tr><td colspan="6" class="py-16 text-center text-slate-500 font-medium">Không tìm thấy công việc nào thỏa mãn điều kiện lọc.</td></tr>
            </tbody>
          </table>
        </div>
        
        <div v-if="filteredAllTasks.length > 0" class="px-6 py-4 border-t border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 flex items-center justify-between shrink-0">
          <span class="text-sm font-medium text-slate-500 dark:text-slate-400">Hiển thị <span class="font-bold text-slate-800 dark:text-white">{{ (currentPage - 1) * itemsPerPage + 1 }}</span> - <span class="font-bold text-slate-800 dark:text-white">{{ Math.min(currentPage * itemsPerPage, filteredAllTasks.length) }}</span> trong <span class="font-bold text-slate-800 dark:text-white">{{ filteredAllTasks.length }}</span> việc</span>
          <div class="flex items-center space-x-2">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-600 text-sm font-bold text-slate-600 dark:text-slate-300 disabled:opacity-40 hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors">Trước</button>
            <span class="px-3 text-sm font-bold text-slate-700 dark:text-slate-300">Trang {{ currentPage }} / {{ totalPages }}</span>
            <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages" class="px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-600 text-sm font-bold text-slate-600 dark:text-slate-300 disabled:opacity-40 hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors">Sau</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="viewMode === 'calendar'" class="flex-1 min-h-0 overflow-hidden px-6 pb-6 pt-4 flex flex-col w-full bg-slate-50/30 dark:bg-[#0f172a]/30">
      <div class="bg-white dark:bg-slate-800 rounded-3xl shadow-sm border border-slate-200 dark:border-slate-700 overflow-hidden flex flex-col flex-1 h-full">
        
        <div class="flex items-center justify-between p-5 border-b border-slate-200 dark:border-slate-700 shrink-0">
          <h2 class="text-xl font-black text-slate-800 dark:text-white flex items-center">
            📅 Tháng {{ currentMonth + 1 }}, {{ currentYear }}
          </h2>
          <div class="flex space-x-2">
            <button @click="prevMonth" class="p-2 rounded-xl bg-slate-100 dark:bg-slate-700 hover:bg-slate-200 dark:hover:bg-slate-600 transition-colors"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg></button>
            <button @click="nextMonth" class="p-2 rounded-xl bg-slate-100 dark:bg-slate-700 hover:bg-slate-200 dark:hover:bg-slate-600 transition-colors"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg></button>
          </div>
        </div>

        <div class="flex-1 flex flex-col min-h-0">
          
          <div class="grid grid-cols-7 text-center border-b border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-800/80 shrink-0">
            <div v-for="day in ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']" :key="day" 
                 class="py-3 text-xs font-black uppercase tracking-widest border-r border-slate-300 dark:border-slate-600 last:border-r-0"
                 :class="(day === 'T7' || day === 'CN') ? 'bg-red-50/50 dark:bg-red-900/10 text-red-500' : 'text-slate-500 dark:text-slate-400'">
              {{ day }}
            </div>
          </div>
          
          <div class="flex-1 overflow-y-auto custom-scrollbar bg-slate-50/20 dark:bg-transparent">
            <div class="grid grid-cols-7">
              <div v-for="(day, idx) in calendarDays" :key="idx" 
                   class="border-b border-r border-slate-300 dark:border-slate-600 p-2 transition-colors relative group flex flex-col min-h-[80px]"
                   :class="[
                     day.isCurrentMonth ? 'bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800' : 'bg-slate-100/50 dark:bg-[#0b1120] text-slate-400',
                     (idx % 7 === 5 || idx % 7 === 6) && day.isCurrentMonth ? 'bg-red-50/10 dark:bg-red-900/5' : ''
                   ]">
                
                <span class="text-sm font-bold w-7 h-7 flex items-center justify-center rounded-full mb-1.5 shrink-0" 
                      :class="isToday(day.date) ? 'bg-blue-600 text-white shadow-md' : 'text-slate-600 dark:text-slate-300'">
                  {{ day.date.getDate() }}
                </span>

                <div class="flex flex-col gap-1 flex-1">
                  <div v-for="task in day.tasks" :key="task.id" @click="openEditModal(task)" 
                       class="px-2 py-1 rounded-lg text-xs font-bold cursor-pointer truncate transition-transform hover:-translate-y-px shadow-sm border flex items-center"
                       :class="getCalendarTaskStyle(task.status)">
                    
                    <span v-if="task.isStart && task.isEnd" class="bg-blue-500 text-white px-1.5 py-0.5 rounded-[4px] mr-1.5 text-[8px] tracking-wider shrink-0">TRONG NGÀY</span>
                    <span v-else-if="task.isStart" class="bg-emerald-500 text-white px-1.5 py-0.5 rounded-[4px] mr-1.5 text-[8px] tracking-wider shrink-0">BẮT ĐẦU</span>
                    <span v-else-if="task.isEnd" class="bg-red-500 text-white px-1.5 py-0.5 rounded-[4px] mr-1.5 text-[8px] tracking-wider shrink-0">HẠN CHÓT</span>
                    
                    <span class="truncate">{{ task.title }}</span>
                  </div>
                </div>

              </div>
            </div>
          </div>
          </div>
      </div>
    </div>

    <div v-if="showFilters" @click="showFilters = false" class="fixed inset-0 bg-slate-900/20 dark:bg-black/40 backdrop-blur-sm z-40 transition-opacity"></div>
    <aside :class="showFilters ? 'translate-x-0' : 'translate-x-full'" class="fixed top-0 right-0 w-80 lg:w-[400px] h-screen bg-white dark:bg-slate-800 shadow-2xl z-50 transition-transform duration-300 ease-in-out border-l border-slate-200 dark:border-slate-700 flex flex-col">
      
      <div class="h-16 px-6 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between shrink-0 bg-slate-50 dark:bg-slate-800/50">
        <h3 class="font-black text-slate-800 dark:text-white flex items-center">
          <span class="text-xl mr-2">🎛️</span> Lọc & Sắp Xếp
        </h3>
        <button @click="showFilters = false" class="p-2 rounded-full text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-700 hover:text-slate-600 dark:hover:text-white transition-colors outline-none">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>

      <div class="flex-1 overflow-y-auto p-6 space-y-8 custom-scrollbar">
        
        <div>
          <label class="block text-[11px] font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-3">Sắp xếp công việc</label>
          <div class="flex items-center space-x-2">
            <div class="relative flex-1">
              <select v-model="sortBy" @change="applySort" class="w-full pl-9 pr-8 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900 text-sm font-bold outline-none focus:border-blue-500 dark:text-slate-300 transition-all appearance-none cursor-pointer text-blue-600 dark:text-blue-400">
                <option value="">↕️ Mặc định</option>
                <option value="deadline">📅 Theo Hạn chót</option>
                <option value="priority">🔥 Theo Độ ưu tiên</option>
                <option value="name">🔤 Theo Tên (A-Z)</option>
              </select>
              <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none opacity-80">↕️</span>
            </div>
            <button v-if="sortBy" @click="toggleSortOrder" class="p-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900 text-sm hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors focus:outline-none shadow-sm font-bold text-slate-600 dark:text-slate-300" :title="sortDesc ? 'Đang xếp Giảm dần' : 'Đang xếp Tăng dần'">
               {{ sortDesc ? '⬇️ Giảm' : '⬆️ Tăng' }}
            </button>
          </div>
        </div>

        <hr class="border-slate-100 dark:border-slate-700" />

        <div class="space-y-5">
          <label class="block text-[11px] font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest">Lọc kết quả</label>
          
          <div>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">🏷️</span>
              <input v-model="searchTag" type="text" placeholder="Nhập tên nhãn (VD: giao diện)..." class="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm font-medium outline-none focus:border-blue-500 dark:text-slate-300 transition-all shadow-sm" />
            </div>
          </div>

          <div>
            <div class="relative">
              <select v-model="searchAssignee" class="w-full pl-10 pr-8 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm font-medium outline-none focus:border-blue-500 dark:text-slate-300 transition-all appearance-none cursor-pointer shadow-sm">
                <option value="">👤 Mọi thành viên</option>
                <option value="unassigned">👻 Chưa phân công</option>
                <option v-for="user in projectMembers" :key="user.id" :value="user.id">{{ user.fullName }}</option>
              </select>
              <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none opacity-60">👥</span>
            </div>
          </div>

          <div>
            <div class="relative">
              <select v-model="searchDeadline" class="w-full pl-10 pr-8 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm font-medium outline-none focus:border-blue-500 dark:text-slate-300 transition-all appearance-none cursor-pointer shadow-sm" :class="searchDeadline === 'overdue' ? 'text-red-500 border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/20' : ''">
                <option value="">⏳ Mọi thời hạn</option>
                <option value="overdue">🔴 Đã trễ hạn</option>
                <option value="today">🟡 Tới hạn hôm nay</option>
                <option value="week">🔵 Trong 7 ngày tới</option>
              </select>
              <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none opacity-60">📅</span>
            </div>
          </div>
        </div>
      </div>

      <div class="p-4 border-t border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50">
        <button @click="clearFilters" class="w-full py-3 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 font-bold rounded-xl hover:bg-slate-100 dark:hover:bg-slate-700 transition-all flex justify-center items-center shadow-sm">
          <span class="mr-2">🗑️</span> Xóa tất cả Lọc & Sắp xếp
        </button>
      </div>
    </aside>

    <div v-if="pendingMove" class="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex items-center justify-center z-[60] p-4">
      <div class="bg-white dark:bg-slate-800 rounded-3xl shadow-2xl w-full max-w-md p-8 text-center animate-fade-in border border-slate-200 dark:border-slate-700">
        <div class="w-16 h-16 bg-blue-50 dark:bg-blue-900/50 text-blue-600 flex items-center justify-center rounded-full mx-auto mb-5 text-3xl shadow-inner">📦</div>
        <h3 class="text-2xl font-black text-slate-800 dark:text-white mb-3">Chuyển trạng thái?</h3>
        <p class="text-slate-500 dark:text-slate-400 mb-8 leading-relaxed">Xác nhận chuyển <br/><b class="text-slate-800 dark:text-slate-200 text-lg">"{{ pendingMove.task.title }}"</b> <br/>sang cột <b class="text-blue-600 dark:text-blue-400 uppercase tracking-wide">{{ formatStatus(pendingMove.newStatus) }}</b>?</p>
        <div class="flex space-x-4">
          <button @click="cancelTaskMove" class="flex-1 py-3 bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 font-bold rounded-xl hover:bg-slate-200 dark:hover:bg-slate-600 transition-all border border-transparent hover:border-slate-300">Hủy</button>
          <button @click="confirmTaskMove" class="flex-1 py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-500/30 hover:-translate-y-0.5">Xác nhận ngay</button>
        </div>
      </div>
    </div>

    <TaskCreateModal v-if="showTaskModal" :projectId="projectId" :projectMembers="projectMembers" :initialColumn="columnForNewTask" @close="showTaskModal = false" @created="onTaskCreated" />
   <TaskDetailModal 
        v-if="showEditModal" 
        :task="editTaskData" 
        :projectMembers="projectMembers" 
        :userRole="userRole" 
        @close="showEditModal = false" 
        @updated="onTaskUpdated" 
    />
  </div>
</template>

<script setup>
import TaskCreateModal from "@/components/TaskCreateModal.vue";
import TaskDetailModal from "@/components/TaskDetailModal.vue";
import { ref, computed, onMounted, watch } from "vue";
import draggable from "vuedraggable";
import { useToast } from "../composables/useToast";

const props = defineProps({ projectId: { type: String, required: true }, userRole: { type: String, required: true } });
const { addToast } = useToast();

const viewMode = ref('board'); 
const searchQuery = ref("");
const searchTag = ref("");
const searchAssignee = ref("");
const searchDeadline = ref(""); 

// 🟢 BIẾN CHO PANEL TRƯỢT & SẮP XẾP & GIỌNG NÓI 🟢
const showFilters = ref(false);
const sortBy = ref("");
const sortDesc = ref(false);
const isListening = ref(false);

const activeFilterCount = computed(() => {
  let count = 0;
  if (searchTag.value) count++;
  if (searchAssignee.value) count++;
  if (searchDeadline.value) count++;
  if (sortBy.value) count++;
  return count;
});

const kanbanColumns = ref([
  { id: 'TODO', title: 'Cần làm', colorClass: 'bg-slate-400' },
  { id: 'IN_PROGRESS', title: 'Đang thực hiện', colorClass: 'bg-blue-500' },
  { id: 'DONE', title: 'Đã hoàn thành', colorClass: 'bg-emerald-500' },
  { id: 'CANCEL', title: 'Đã hủy', colorClass: 'bg-red-500' }
]);

const boardTasks = ref({ 'TODO': [], 'IN_PROGRESS': [], 'DONE': [], 'CANCEL': [] });

// 🟢 LOGIC TÌM KIẾM GIỌNG NÓI (NATIVE CHROME API) 🟢
const startVoiceSearch = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) {
    addToast("Trình duyệt của bạn không hỗ trợ tìm kiếm giọng nói. Hãy dùng Chrome!", "warning");
    return;
  }
  
  if (isListening.value) return; 

  const recognition = new SpeechRecognition();
  recognition.lang = 'vi-VN';
  recognition.interimResults = false; 
  recognition.maxAlternatives = 1;

  recognition.onstart = () => { isListening.value = true; };
  
  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    searchQuery.value = transcript.replace(/[.,!?]$/, '').trim();
  };
  
  recognition.onerror = (event) => { console.error("Lỗi giọng nói: ", event.error); isListening.value = false; };
  recognition.onend = () => { isListening.value = false; };
  
  recognition.start();
};

const clearFilters = () => { 
  searchQuery.value = ""; searchTag.value = ""; searchAssignee.value = ""; searchDeadline.value = ""; 
  sortBy.value = ""; sortDesc.value = false; applySort(); 
  showFilters.value = false; // Đóng panel sau khi xóa
};

const matchFilter = (task) => {
  let matchName = true, matchTag = true, matchAssignee = true, matchDeadline = true;
  if (searchQuery.value.trim()) matchName = task.title.toLowerCase().includes(searchQuery.value.trim().toLowerCase());
  if (searchTag.value.trim()) matchTag = task.tags ? task.tags.toLowerCase().includes(searchTag.value.trim().toLowerCase()) : false;
  if (searchAssignee.value) {
    if (searchAssignee.value === 'unassigned') matchAssignee = !task.assigneeIds || task.assigneeIds.trim() === '';
    else matchAssignee = task.assigneeIds ? task.assigneeIds.split(',').includes(String(searchAssignee.value)) : false;
  }
  if (searchDeadline.value) {
    if (!task.deadline || task.deadline === 'null') matchDeadline = false;
    else {
      const today = new Date(); today.setHours(0, 0, 0, 0);
      const taskDate = new Date(task.deadline); taskDate.setHours(0, 0, 0, 0);
      const diffDays = Math.ceil((taskDate - today) / (1000 * 60 * 60 * 24));
      if (task.status === 'DONE' || task.status === 'CANCEL') matchDeadline = false;
      else if (searchDeadline.value === 'overdue') matchDeadline = diffDays < 0;
      else if (searchDeadline.value === 'today') matchDeadline = diffDays === 0;
      else if (searchDeadline.value === 'week') matchDeadline = diffDays >= 0 && diffDays <= 7;
    }
  }
  return matchName && matchTag && matchAssignee && matchDeadline;
};

// 🟢 LOGIC SẮP XẾP TOÀN CỤC 🟢
const applySort = () => {
  if (!sortBy.value) { fetchTasks(); return; }

  const priorityMap = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1, null: 0, undefined: 0 };

  const sortFunc = (a, b) => {
    let valA, valB;
    if (sortBy.value === 'deadline') {
      valA = (!a.deadline || a.deadline === 'null') ? Infinity : new Date(a.deadline).getTime();
      valB = (!b.deadline || b.deadline === 'null') ? Infinity : new Date(b.deadline).getTime();
    } else if (sortBy.value === 'priority') {
      valA = priorityMap[a.priority] || 0;
      valB = priorityMap[b.priority] || 0;
    } else if (sortBy.value === 'name') {
      valA = a.title.toLowerCase();
      valB = b.title.toLowerCase();
    }

    if (valA < valB) return sortDesc.value ? 1 : -1;
    if (valA > valB) return sortDesc.value ? -1 : 1;
    return 0;
  };

  ['TODO', 'IN_PROGRESS', 'DONE', 'CANCEL'].forEach(col => {
    if(boardTasks.value[col]) boardTasks.value[col].sort(sortFunc);
  });
};

const toggleSortOrder = () => { sortDesc.value = !sortDesc.value; applySort(); };

const getAssigneeArray = (namesStr) => !namesStr ? [] : namesStr.split(',').map(n => n.trim()).filter(n => n);
const getTagsArray = (tagsStr) => !tagsStr ? [] : tagsStr.split(',').map(t => t.trim()).filter(t => t);
const getTagColor = (tag) => {
  const str = tag.trim().toLowerCase();
  const colors = [ 'bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-400 border border-red-200 dark:border-red-800/50', 'bg-blue-100 text-blue-600 dark:bg-blue-900/40 dark:text-blue-400 border border-blue-200 dark:border-blue-800/50', 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/40 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800/50', 'bg-purple-100 text-purple-600 dark:bg-purple-900/40 dark:text-purple-400 border border-purple-200 dark:border-purple-800/50', 'bg-amber-100 text-amber-600 dark:bg-amber-900/40 dark:text-amber-400 border border-amber-200 dark:border-amber-800/50', 'bg-pink-100 text-pink-600 dark:bg-pink-900/40 dark:text-pink-400 border border-pink-200 dark:border-pink-800/50', 'bg-cyan-100 text-cyan-600 dark:bg-cyan-900/40 dark:text-cyan-400 border border-cyan-200 dark:border-cyan-800/50', 'bg-indigo-100 text-indigo-600 dark:bg-indigo-900/40 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800/50' ];
  let hash = 0; for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash);
  return colors[Math.abs(hash) % colors.length];
};

const allTasks = computed(() => [ ...(boardTasks.value['TODO'] || []), ...(boardTasks.value['IN_PROGRESS'] || []), ...(boardTasks.value['DONE'] || []), ...(boardTasks.value['CANCEL'] || []) ]);

const filteredAllTasks = computed(() => {
  let filtered = allTasks.value.filter(task => matchFilter(task));
  if (sortBy.value) {
    const priorityMap = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1, null: 0, undefined: 0 };
    filtered.sort((a, b) => {
      let valA, valB;
      if (sortBy.value === 'deadline') {
        valA = (!a.deadline || a.deadline === 'null') ? Infinity : new Date(a.deadline).getTime();
        valB = (!b.deadline || b.deadline === 'null') ? Infinity : new Date(b.deadline).getTime();
      } else if (sortBy.value === 'priority') {
        valA = priorityMap[a.priority] || 0;
        valB = priorityMap[b.priority] || 0;
      } else if (sortBy.value === 'name') {
        valA = a.title.toLowerCase();
        valB = b.title.toLowerCase();
      }
      if (valA < valB) return sortDesc.value ? 1 : -1;
      if (valA > valB) return sortDesc.value ? -1 : 1;
      return 0;
    });
  }
  return filtered;
});

const currentPage = ref(1);
const itemsPerPage = ref(7); 
const totalPages = computed(() => Math.ceil(filteredAllTasks.value.length / itemsPerPage.value) || 1);
const paginatedTasks = computed(() => { const start = (currentPage.value - 1) * itemsPerPage.value; return filteredAllTasks.value.slice(start, start + itemsPerPage.value); });
const changePage = (page) => { if (page >= 1 && page <= totalPages.value) currentPage.value = page; };
watch(filteredAllTasks, () => { if (currentPage.value > totalPages.value) currentPage.value = totalPages.value; });

// LOGIC DẠNG LỊCH (CALENDAR)
const currentDate = ref(new Date());
const currentYear = computed(() => currentDate.value.getFullYear());
const currentMonth = computed(() => currentDate.value.getMonth());

const calendarDays = computed(() => {
  const year = currentYear.value;
  const month = currentMonth.value;
  const firstDayOfMonth = new Date(year, month, 1);
  const lastDayOfMonth = new Date(year, month + 1, 0);
  
  let startDayOfWeek = firstDayOfMonth.getDay() - 1;
  if (startDayOfWeek === -1) startDayOfWeek = 6; 

  const days = [];
  for (let i = startDayOfWeek; i > 0; i--) {
    const d = new Date(year, month, 1 - i);
    days.push({ date: d, isCurrentMonth: false, tasks: getTasksForDate(d) });
  }
  for (let i = 1; i <= lastDayOfMonth.getDate(); i++) {
    const d = new Date(year, month, i);
    days.push({ date: d, isCurrentMonth: true, tasks: getTasksForDate(d) });
  }
  const remainingCells = (days.length > 35 ? 42 : 35) - days.length;
  for (let i = 1; i <= remainingCells; i++) {
    const d = new Date(year, month + 1, i);
    days.push({ date: d, isCurrentMonth: false, tasks: getTasksForDate(d) });
  }
  return days;
});

const getTasksForDate = (date) => {
  const dString = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  const tasksOnDay = [];
  filteredAllTasks.value.forEach(t => {
    const isStart = t.startDate === dString;
    const isEnd = t.deadline === dString;

    if (isStart || isEnd) {
      tasksOnDay.push({ ...t, isStart, isEnd });
    }
  });
  return tasksOnDay;
};

const isToday = (date) => {
  const today = new Date();
  return date.getDate() === today.getDate() && date.getMonth() === today.getMonth() && date.getFullYear() === today.getFullYear();
};

const prevMonth = () => { currentDate.value = new Date(currentYear.value, currentMonth.value - 1, 1); };
const nextMonth = () => { currentDate.value = new Date(currentYear.value, currentMonth.value + 1, 1); };

const getCalendarTaskStyle = (status) => {
  if (status === 'DONE') return 'bg-emerald-100 text-emerald-700 border-emerald-200 dark:bg-emerald-900/40 dark:text-emerald-400 dark:border-emerald-800 opacity-70';
  if (status === 'IN_PROGRESS') return 'bg-blue-100 text-blue-700 border-blue-200 dark:bg-blue-900/40 dark:text-blue-400 dark:border-blue-800';
  if (status === 'CANCEL') return 'bg-red-100 text-red-700 border-red-200 dark:bg-red-900/40 dark:text-red-400 dark:border-red-800 opacity-50';
  return 'bg-white text-slate-700 border-slate-200 dark:bg-slate-700 dark:text-slate-300 dark:border-slate-600';
};

const showTaskModal = ref(false);
const columnForNewTask = ref('TODO'); 
const showEditModal = ref(false);
const editTaskData = ref(null);
const projectMembers = ref([]);
const pendingMove = ref(null);

onMounted(() => { fetchTasks(); fetchProjectMembers(); });

const fetchTasks = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/tasks/list?projectId=${props.projectId}`);
    if (res.ok) {
        const data = await res.json();
        boardTasks.value = { 'TODO': [], 'IN_PROGRESS': [], 'DONE': [], 'CANCEL': [] };
        data.forEach(task => { boardTasks.value[task.status] ? boardTasks.value[task.status].push(task) : boardTasks.value['TODO'].push(task); });
        if (sortBy.value) applySort();
    }
  } catch (error) {}
};

const fetchProjectMembers = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/projects/members-list?projectId=${props.projectId}`);
    if (res.ok) projectMembers.value = await res.json();
  } catch (error) {}
};

const saveColumnOrder = async (columnId) => {
  const taskIds = boardTasks.value[columnId].map(t => t.id).join(',');
  try { await fetch("http://localhost:8080/api/tasks/update-order", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ taskIds: taskIds }) }); } catch (error) {}
};

const onTaskMoved = (event, columnId) => {
  if (event.added) pendingMove.value = { task: event.added.element, newStatus: columnId, oldStatus: event.added.element.status };
  if (event.moved && !sortBy.value) saveColumnOrder(columnId);
};

const confirmTaskMove = async () => {
  if (!pendingMove.value) return;
  const { task, newStatus, oldStatus } = pendingMove.value;
  try {
    task.status = newStatus; 
    const currentUserId = localStorage.getItem("userId") || 1; 
    await fetch("http://localhost:8080/api/tasks/update-status", { method: "POST", headers: { "Content-Type": "application/json", "User-ID": currentUserId }, body: JSON.stringify({ taskId: task.id, status: newStatus, oldStatus: oldStatus }) });
    if (!sortBy.value) await saveColumnOrder(newStatus);
    addToast("Chuyển trạng thái thành công!", "success");
  } catch (error) {}
  pendingMove.value = null; 
};
const cancelTaskMove = () => { pendingMove.value = null; fetchTasks(); };

const openTaskModal = (columnId) => { columnForNewTask.value = columnId; showTaskModal.value = true; };
const onTaskCreated = () => { showTaskModal.value = false; addToast("Tạo công việc thành công!", "success"); fetchTasks(); };
const openEditModal = (task) => { editTaskData.value = task; showEditModal.value = true; };
const onTaskUpdated = () => { showEditModal.value = false; addToast("Đã cập nhật công việc!", "success"); fetchTasks(); };

const getDeadlineBadge = (deadline, status) => {
  if (!deadline || deadline === 'null') return null;
  if (status === 'DONE' || status === 'CANCEL') return null; 
  
  const today = new Date(); 
  today.setHours(0, 0, 0, 0);
  const taskDate = new Date(deadline); 
  taskDate.setHours(0, 0, 0, 0);
  
  const diffDays = Math.ceil((taskDate - today) / (1000 * 60 * 60 * 24));
  
  if (diffDays < 0) return { text: `Quá hạn ${Math.abs(diffDays)} ngày`, icon: '🚨', class: 'bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-400 border border-red-200 dark:border-red-800 animate-pulse' };
  if (diffDays === 0) return { text: 'Tới hạn hôm nay', icon: '⚠️', class: 'bg-orange-100 text-orange-700 dark:bg-orange-900/40 dark:text-orange-400 border border-orange-200 dark:border-orange-800' };
  if (diffDays <= 3) return { text: `Còn ${diffDays} ngày`, icon: '⏳', class: 'bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-400 border border-amber-200 dark:border-amber-800' };
  return { text: `Còn ${diffDays} ngày`, icon: '📅', class: 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300 border border-slate-200 dark:border-slate-600' };
};

const formatDateFull = (d) => {
  if (!d || d === 'null') return '';
  const parts = d.split('-');
  if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
  return d;
};

const formatStatus = (s) => s==='TODO'?'Cần làm':s==='IN_PROGRESS'?'Đang làm':s==='DONE'?'Hoàn thành':'Đã hủy';
const getStatusStyle = (s) => s==='TODO'?'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300':s==='IN_PROGRESS'?'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400':s==='DONE'?'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400':'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400';
const formatPriority = (p) => p === 'HIGH' ? 'Cao' : p === 'MEDIUM' ? 'Trung bình' : 'Thấp';
const getPriorityTag = (p) => p === 'HIGH' ? 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400' : p === 'MEDIUM' ? 'bg-orange-100 text-orange-600 dark:bg-orange-900/30 dark:text-orange-400' : 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300';
const getPriorityBorder = (p) => p === 'HIGH' ? 'border-l-red-500' : p === 'MEDIUM' ? 'border-l-orange-400' : 'border-l-slate-300 dark:border-l-slate-600';
const formatDate = (d) => (!d || d === 'null') ? '' : d.split('-').length === 3 ? `${d.split('-')[2]}/${d.split('-')[1]}` : d;
const isDeadlineNear = (d) => { if (!d || d === 'null') return false; const left = (new Date(d) - new Date()) / (1000 * 60 * 60 * 24); return left > 0 && left <= 3; };
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.custom-scrollbar::-webkit-scrollbar { width: 5px; height: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #334155; }
</style>