/**
 * 公共格式化工具函数
 */

/**
 * 题目类型映射
 */
const QUESTION_TYPE_MAP: Record<string, string> = {
  SINGLE_CHOICE: '单选题',
  MULTIPLE_CHOICE: '多选题',
  TRUE_FALSE: '判断题',
  FILL_BLANK: '填空题',
  ESSAY: '简答题'
}

/**
 * 题目类型颜色映射
 */
const QUESTION_TYPE_COLOR_MAP: Record<string, string> = {
  SINGLE_CHOICE: 'primary',
  MULTIPLE_CHOICE: 'success',
  TRUE_FALSE: 'warning',
  FILL_BLANK: 'info',
  ESSAY: 'danger'
}

/**
 * 难度映射
 */
const DIFFICULTY_MAP: Record<string, string> = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难'
}

/**
 * 难度颜色映射
 */
const DIFFICULTY_COLOR_MAP: Record<string, string> = {
  EASY: 'success',
  MEDIUM: 'warning',
  HARD: 'danger'
}

/**
 * 考试状态映射
 */
const EXAM_STATUS_MAP: Record<string, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  STARTED: '进行中',
  ENDED: '已结束',
  CANCELLED: '已取消'
}

/**
 * 考试状态颜色映射
 */
const EXAM_STATUS_COLOR_MAP: Record<string, string> = {
  DRAFT: 'info',
  PUBLISHED: 'success',
  STARTED: 'primary',
  ENDED: 'warning',
  CANCELLED: 'danger'
}

/**
 * 用户角色映射
 */
const ROLE_MAP: Record<string, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生'
}

/**
 * 用户角色颜色映射
 */
const ROLE_COLOR_MAP: Record<string, string> = {
  ADMIN: 'danger',
  TEACHER: 'warning',
  STUDENT: 'success'
}

/**
 * 用户状态映射
 */
const USER_STATUS_MAP: Record<string, string> = {
  ACTIVE: '正常',
  INACTIVE: '未激活',
  SUSPENDED: '已禁用'
}

/**
 * 考试会话状态映射
 */
const SESSION_STATUS_MAP: Record<string, string> = {
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  SUBMITTED: '已提交',
  GRADED: '已评分'
}

/**
 * 考试会话状态颜色映射
 */
const SESSION_STATUS_COLOR_MAP: Record<string, string> = {
  NOT_STARTED: 'info',
  IN_PROGRESS: 'warning',
  SUBMITTED: 'primary',
  GRADED: 'success'
}

/**
 * 评分状态映射
 */
const GRADING_STATUS_MAP: Record<string, string> = {
  PENDING: '待评分',
  GRADING: '评分中',
  COMPLETED: '已完成'
}

/**
 * 评分状态颜色映射
 */
const GRADING_STATUS_COLOR_MAP: Record<string, string> = {
  PENDING: 'warning',
  GRADING: 'primary',
  COMPLETED: 'success'
}

/**
 * 获取题目类型名称
 */
export function getTypeName(type: string): string {
  return QUESTION_TYPE_MAP[type] || type
}

/**
 * 获取题目类型颜色
 */
export function getTypeColor(type: string): string {
  return QUESTION_TYPE_COLOR_MAP[type] || ''
}

/**
 * 获取难度名称
 */
export function getDifficultyName(difficulty: string): string {
  return DIFFICULTY_MAP[difficulty] || difficulty
}

/**
 * 获取难度颜色
 */
export function getDifficultyColor(difficulty: string): string {
  return DIFFICULTY_COLOR_MAP[difficulty] || ''
}

/**
 * 获取考试状态名称
 */
export function getStatusName(status: string): string {
  return EXAM_STATUS_MAP[status] || status
}

/**
 * 获取考试状态颜色
 */
export function getStatusColor(status: string): string {
  return EXAM_STATUS_COLOR_MAP[status] || ''
}

/**
 * 获取用户角色名称
 */
export function getRoleName(role: string): string {
  return ROLE_MAP[role] || role
}

/**
 * 获取用户角色颜色
 */
export function getRoleColor(role: string): string {
  return ROLE_COLOR_MAP[role] || ''
}

/**
 * 获取用户状态名称
 */
export function getUserStatusName(status: string): string {
  return USER_STATUS_MAP[status] || status
}

/**
 * 获取考试会话状态名称
 */
export function getSessionStatusName(status: string): string {
  return SESSION_STATUS_MAP[status] || status
}

/**
 * 获取考试会话状态颜色
 */
export function getSessionStatusColor(status: string): string {
  return SESSION_STATUS_COLOR_MAP[status] || ''
}

/**
 * 获取评分状态名称
 */
export function getGradingStatusName(status?: string): string {
  if (!status) return '-'
  return GRADING_STATUS_MAP[status] || status
}

/**
 * 获取评分状态颜色
 */
export function getGradingStatusColor(status?: string): string {
  if (!status) return 'info'
  return GRADING_STATUS_COLOR_MAP[status] || ''
}

/**
 * 格式化日期时间
 * @param date 日期字符串或 Date 对象
 * @param format 格式类型：'full' | 'date' | 'time' | 'datetime'
 */
export function formatDate(date?: string | Date | null, format: 'full' | 'date' | 'time' | 'datetime' = 'datetime'): string {
  if (!date) return '-'
  
  const d = typeof date === 'string' ? new Date(date) : date
  
  if (isNaN(d.getTime())) return '-'
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  switch (format) {
    case 'date':
      return `${year}-${month}-${day}`
    case 'time':
      return `${hours}:${minutes}:${seconds}`
    case 'full':
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    case 'datetime':
    default:
      return d.toLocaleString('zh-CN')
  }
}

/**
 * 格式化持续时间（分钟转为可读格式）
 */
export function formatDuration(minutes: number): string {
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (mins === 0) {
    return `${hours}小时`
  }
  return `${hours}小时${mins}分钟`
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i]
}

/**
 * 格式化数字（添加千分位）
 */
export function formatNumber(num: number): string {
  return num.toLocaleString('zh-CN')
}

/**
 * 格式化百分比
 */
export function formatPercent(value: number, decimals: number = 1): string {
  return value.toFixed(decimals) + '%'
}

export default {
  getTypeName,
  getTypeColor,
  getDifficultyName,
  getDifficultyColor,
  getStatusName,
  getStatusColor,
  getRoleName,
  getRoleColor,
  getUserStatusName,
  getSessionStatusName,
  getSessionStatusColor,
  getGradingStatusName,
  getGradingStatusColor,
  formatDate,
  formatDuration,
  formatFileSize,
  formatNumber,
  formatPercent
}
