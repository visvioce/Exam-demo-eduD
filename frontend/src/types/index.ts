// 用户相关类型
export interface User {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: 'ADMIN' | 'TEACHER' | 'STUDENT'
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  createdAt?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
}

export interface LoginResponse {
  token: string
  user: UserResponse
}

export interface UserResponse {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: User['role']
  status: User['status']
  createdAt?: string
}

// 课程相关类型
export interface Course {
  id: number
  name: string
  code: string
  description?: string
  coverUrl?: string
  teacherId: number
  teacherName?: string
  credits: number
  status: 'ACTIVE' | 'INACTIVE'
  createdAt?: string
  deadline?: string
}

// 题目相关类型
export interface Question {
  id: number
  content: string
  type: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'FILL_BLANK' | 'ESSAY'
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  score: number
  teacherId: number
  subject: string
  options?: QuestionOption[]
  correctAnswer?: string | string[] | boolean | null
  scoringCriteria?: ScoringCriterion[]
  explanation?: string
  blankCount?: number
}

// 考试题目（学生视角，不含答案）
export type QuestionForExam = Omit<Question, 'correctAnswer' | 'explanation' | 'scoringCriteria'>

export interface QuestionOption {
  id: string
  text: string
}

export interface ScoringCriterion {
  point: string
  score: number
}

// 试卷相关类型
export interface Paper {
  id: number
  name: string
  description?: string
  courseId: number
  courseName?: string
  teacherId: number
  questions: PaperQuestion[]
  totalScore: number
  type: 'MANUAL' | 'AUTO'
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
}

export interface TypeConfig {
  count: number
  score: number
  difficulty?: string
  subject?: string
}

export interface AutoGeneratePaperRequest {
  name: string
  description?: string
  courseId: number
  subject?: string
  difficulty?: string
  singleChoice?: TypeConfig
  multipleChoice?: TypeConfig
  trueFalse?: TypeConfig
  fillBlank?: TypeConfig
  essay?: TypeConfig
  singleChoiceCount?: number
  singleChoiceScore?: number
  multipleChoiceCount?: number
  multipleChoiceScore?: number
  trueFalseCount?: number
  trueFalseScore?: number
  fillBlankCount?: number
  fillBlankScore?: number
  essayCount?: number
  essayScore?: number
}

export interface PaperQuestion {
  questionId: number
  score: number
}

// 考试相关类型
export interface Exam {
  id: number
  title: string
  description?: string
  courseId: number
  courseName?: string
  paperId: number
  teacherId: number
  teacherName?: string
  startedAt: string
  endedAt: string
  duration: number
  totalScore: number
  passScore: number
  status: 'DRAFT' | 'PUBLISHED' | 'STARTED' | 'ENDED' | 'CANCELLED'
  createdAt?: string
  studentExamStatus?: 'NOT_STARTED' | 'IN_PROGRESS' | 'SUBMITTED' | 'GRADED'
}

// 考试记录相关类型
export interface ExamSession {
  id: number
  examId: number
  studentId: number
  studentName?: string
  startedAt: string
  submittedAt?: string
  score?: number
  totalScore: number
  answers?: Answer[]
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'SUBMITTED' | 'GRADED'
  gradingStatus?: 'PENDING' | 'GRADING' | 'COMPLETED'
}

export interface Answer {
  questionId: number
  answer: string | string[] | null
  isCorrect?: boolean
  score?: number
  questionType?: string
  gradingStatus?: 'PENDING' | 'GRADED'
  teacherComment?: string
}

// 公告相关类型
export interface Announcement {
  id: number
  title: string
  content: string
  type: 'SYSTEM' | 'EXAM' | 'COURSE'
  priority?: 'LOW' | 'MEDIUM' | 'HIGH'
  status?: 'DRAFT' | 'PUBLISHED'
  publisherId: number
  publisherName?: string
  publishedAt?: string
  createdAt?: string
}

// AI 配置相关类型
export interface AiConfig {
  id: number
  userId: number
  name: string
  baseUrl: string
  apiKey: string
  models: string[]  // 模型列表
  activeModel: string | null  // 当前激活的模型
  createdAt?: string
}

// 激活模型信息
export interface ActiveModelInfo {
  configId: number
  configName: string
  baseUrl: string
  apiKey: string
  model: string
}

// 通用响应类型
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageRequest {
  current: number
  size: number
  orderBy?: string
  asc?: boolean
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 轮播图相关类型
export interface Carousel {
  id: number
  title: string
  imageUrl: string
  linkUrl?: string
  description?: string
  sortOrder: number
  status: 'ACTIVE' | 'INACTIVE'
  startAt?: string
  endAt?: string
}

// AI生成题目请求
export interface GenerateQuestionRequest {
  subject: string
  type: string
  difficulty: string
  count: number
  requirements?: string
}

// AI生成题目响应
export interface GeneratedQuestionResponse {
  questions: GeneratedQuestion[]
}

export interface GeneratedQuestion {
  content: string
  type: string
  options?: GeneratedOption[]
  correctAnswer: string | string[] | boolean | null
  explanation?: string
}

export interface GeneratedOption {
  id: string
  text: string
}

// 主观题评分请求
export interface GradeSubjectiveRequest {
  examSessionId: number
  grades: SubjectiveGrade[]
}

export interface SubjectiveGrade {
  questionId: number
  score: number
  comment?: string
}

// 考试结果响应
export interface ExamResultResponse {
  examSessionId: number
  examId: number
  examTitle: string
  studentId: number
  studentName?: string
  startedAt?: string
  submittedAt?: string
  objectiveScore: number
  subjectiveScore: number
  totalScore: number
  maxScore: number
  gradingStatus: 'PENDING' | 'GRADING' | 'GRADED' | 'COMPLETED'
  answers: AnswerDetail[]
}

export interface AnswerDetail {
  questionId: number
  questionContent: string
  questionType: string
  answer: string | string[] | null
  isCorrect?: boolean
  score?: number
  maxScore: number
  gradingStatus?: 'PENDING' | 'GRADED' | 'COMPLETED'
  teacherComment?: string
}
