/**
 * 考试管理 API 模块
 *
 * @module api/exam
 * @description 提供考试相关的所有 API 接口调用
 *
 * 功能分类：
 * - 考试管理（管理员/教师）：创建、编辑、发布、取消、删除
 * - 考试查询（学生）：获取考试列表、查看考试详情
 * - 考试会话：开始考试、提交答卷、获取题目
 * - 成绩管理：查询成绩、评阅答卷
 */
import request from "@/utils/request";
import type {
  Exam,
  ExamSession,
  PageRequest,
  PageResult,
  QuestionForExam,
  ExamResultResponse,
} from "@/types";

/**
 * 考试答案提交类型
 * @property questionId - 题目ID
 * @property answer - 学生答案（单选/判断为字符串，多选为字符串数组）
 * @property questionType - 题目类型（SINGLE_CHOICE/MULTIPLE_CHOICE/TRUE_FALSE/FILL_BLANK/ESSAY）
 */
export interface ExamAnswerSubmit {
  questionId: number;
  answer: string | string[];
  questionType: string;
}

/**
 * 考试 API 对象
 * 所有考试相关接口的封装
 */
export const examApi = {
  /**
   * 分页查询考试列表
   * @param params - 查询参数，包含分页信息和筛选条件（课程ID、教师ID、状态）
   * @returns 分页考试列表
   * @permission 管理员、教师（只能查看自己的）
   */
  page(
    params: PageRequest & {
      courseId?: number;
      teacherId?: number;
      status?: string;
    },
  ) {
    return request.get<PageResult<Exam>>("/exams/page", { params });
  },

  /**
   * 获取所有考试列表
   * @returns 考试列表
   * @permission 管理员
   */
  list() {
    return request.get<Exam[]>("/exams");
  },

  /**
   * 获取已发布的考试列表
   * @returns 已发布的考试列表
   * @permission 学生
   */
  getPublishedExams() {
    return request.get<Exam[]>("/exams/published");
  },

  /**
   * 获取当前登录学生的考试列表
   * @returns 我的考试列表
   * @permission 学生
   */
  getMyExams() {
    return request.get<Exam[]>("/exams/my");
  },

  /**
   * 获取考试详情
   * @param id - 考试ID
   * @returns 考试详情
   */
  getById(id: number) {
    return request.get<Exam>(`/exams/${id}`);
  },

  /**
   * 获取考试的题目（学生考试时使用，不含正确答案）
   * @param id - 考试ID
   * @returns 题目列表（不含正确答案）
   * @permission 已登录且考试已发布
   */
  getQuestions(id: number) {
    return request.get<QuestionForExam[]>(`/exams/${id}/questions`);
  },

  /**
   * 获取考试的试卷信息
   * @param id - 考试ID
   * @returns 试卷信息
   */
  getPaper(id: number) {
    return request.get<import("@/types").Paper>(`/exams/${id}/paper`);
  },

  /**
   * 获取考试回顾所需的完整题目信息（包含正确答案和解析）
   * @param id - 考试ID
   * @returns 完整题目列表（含正确答案和解析）
   * @permission 考试结束后或教师
   */
  getReviewQuestions(id: number) {
    return request.get<import("@/types").Question[]>(`/exams/${id}/review-questions`);
  },

  // 创建考试
  create(data: Partial<Exam>) {
    return request.post<Exam>("/exams", data);
  },

  // 更新考试
  update(id: number, data: Partial<Exam>) {
    return request.put<Exam>(`/exams/${id}`, data);
  },

  // 删除考试
  delete(id: number) {
    return request.delete(`/exams/${id}`);
  },

  // 发布考试
  publish(id: number) {
    return request.post(`/exams/${id}/publish`);
  },

  // 取消考试
  cancel(id: number) {
    return request.post(`/exams/${id}/cancel`);
  },

  // 开始考试
  start(id: number) {
    return request.post<ExamSession>(`/exams/${id}/start`);
  },

  // 提交考试
  submit(id: number, data: ExamAnswerSubmit[]) {
    return request.post(`/exams/${id}/submit`, data);
  },

  // 自动保存答案
  autoSave(id: number, data: ExamAnswerSubmit[]) {
    return request.put(`/exams/${id}/auto-save`, data);
  },
};

export const examSessionApi = {
  // 分页查询考试记录
  page(
    params: PageRequest & {
      examId?: number;
      studentId?: number;
      status?: string;
      gradingStatus?: string;
    },
  ) {
    return request.get<PageResult<ExamSession>>("/exam-sessions/page", {
      params,
    });
  },

  // 获取所有考试记录
  list() {
    return request.get<ExamSession[]>("/exam-sessions");
  },

  // 获取考试记录详情
  getById(id: number) {
    return request.get<ExamSession>(`/exam-sessions/${id}`);
  },

  // 获取某考试的所有记录
  getByExamId(examId: number) {
    return request.get<ExamSession[]>(`/exam-sessions/exam/${examId}`);
  },

  // 获取当前用户的考试记录
  getMySessions() {
    return request.get<ExamSession[]>("/exam-sessions/my");
  },

  // 主观题评分
  gradeSubjectiveAnswers(data: {
    examSessionId: number;
    grades: { questionId: number; score: number; comment?: string }[];
  }) {
    return request.post("/exam-sessions/grade", data);
  },

  // 按考试批量自动阅卷（客观题重评）
  autoGradeByExam(examId: number) {
    return request.post<number>(`/exam-sessions/exam/${examId}/auto-grade`);
  },

  // 获取考试结果详情
  getExamResult(id: number) {
    return request.get<ExamResultResponse>(`/exam-sessions/${id}/result`);
  },
};
