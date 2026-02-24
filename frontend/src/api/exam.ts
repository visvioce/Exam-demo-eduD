import request from "@/utils/request";
import type {
  Exam,
  ExamSession,
  PageRequest,
  PageResult,
  QuestionForExam,
  ExamResultResponse,
} from "@/types";

// 考试答案提交类型
export interface ExamAnswerSubmit {
  questionId: number;
  answer: string | string[];
  questionType: string;
}

export const examApi = {
  // 分页查询考试
  page(
    params: PageRequest & {
      courseId?: number;
      teacherId?: number;
      status?: string;
    },
  ) {
    return request.get<PageResult<Exam>>("/exams/page", { params });
  },

  // 获取所有考试
  list() {
    return request.get<Exam[]>("/exams");
  },

  // 获取已发布的考试
  getPublishedExams() {
    return request.get<Exam[]>("/exams/published");
  },

  // 获取我的考试
  getMyExams() {
    return request.get<Exam[]>("/exams/my");
  },

  // 获取考试详情
  getById(id: number) {
    return request.get<Exam>(`/exams/${id}`);
  },

  // 获取考试的题目（学生可访问，不含正确答案）
  getQuestions(id: number) {
    return request.get<QuestionForExam[]>(`/exams/${id}/questions`);
  },

  // 获取考试的试卷信息
  getPaper(id: number) {
    return request.get<import("@/types").Paper>(`/exams/${id}/paper`);
  },

  // 获取考试回顾所需的完整题目信息（包含正确答案和解析）
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

  // 获取待评分的考试记录
  getPendingGrading() {
    return request.get<ExamSession[]>("/exam-sessions/pending-grading");
  },

  // 获取某考试待评分的记录
  getPendingGradingByExamId(examId: number) {
    return request.get<ExamSession[]>(
      `/exam-sessions/pending-grading/exam/${examId}`,
    );
  },

  // 主观题评分
  gradeSubjectiveAnswers(data: {
    examSessionId: number;
    grades: { questionId: number; score: number; comment?: string }[];
  }) {
    return request.post("/exam-sessions/grade", data);
  },

  // 获取考试结果详情
  getExamResult(id: number) {
    return request.get<ExamResultResponse>(`/exam-sessions/${id}/result`);
  },
};
