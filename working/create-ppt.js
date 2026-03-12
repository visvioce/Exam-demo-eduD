const pptxgenjs = require("pptxgenjs");
const fs = require("fs");

// 创建演示文稿
const pres = new pptxgenjs();

// 设置基本信息
pres.author = "南方职业学院";
pres.company = "南方职业学院";
pres.title = "基于Java的在线考试系统设计与实现 - 开题答辩";

// 定义主题颜色 - 使用 "Ocean Gradient" 调色板
const colors = {
  primary: "065A82",      // 深蓝色
  secondary: "1C7293",    // 青蓝色
  accent: "00A896",       // 海蓝色
  dark: "21295C",         // 午夜蓝
  light: "F8F9FA",        // 浅灰色
  white: "FFFFFF"
};

// 设置默认字体
pres.defineLayout({ name: "A4", width: 10, height: 7.5 });
pres.layout = "A4";

// ========== 第1页：封面页 ==========
const slide1 = pres.addSlide();
slide1.background = { color: colors.primary };

// 标题
slide1.addText("基于 Java 的在线考试系统", {
  x: 1,
  y: 2,
  w: 8,
  h: 1,
  fontSize: 48,
  bold: true,
  color: colors.white,
  align: "center",
  fontFace: "Microsoft YaHei"
});

slide1.addText("设计与实现", {
  x: 1,
  y: 3,
  w: 8,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.white,
  align: "center",
  fontFace: "Microsoft YaHei"
});

// 副标题
slide1.addText("毕业设计开题报告答辩", {
  x: 1,
  y: 3.8,
  w: 8,
  h: 0.6,
  fontSize: 24,
  color: "B4D4E8",
  align: "center",
  fontFace: "Microsoft YaHei"
});

// 底部信息
slide1.addText([
  { text: "答辩人：王正贤\n", options: { fontSize: 18, color: colors.white, bold: true } },
  { text: "学号：12345678\n", options: { fontSize: 18, color: colors.white } },
  { text: "指导老师：导师姓名\n", options: { fontSize: 18, color: colors.white } },
  { text: "专业：计算机科学与技术\n", options: { fontSize: 18, color: colors.white } },
  { text: "答辩日期：2026年3月", options: { fontSize: 18, color: colors.white } }
], {
  x: 1,
  y: 5,
  w: 8,
  h: 1.5,
  align: "center",
  fontFace: "Microsoft YaHei"
});

// ========== 第2页：目录 ==========
const slide2 = pres.addSlide();
slide2.background = { color: colors.light };

slide2.addText("目录", {
  x: 0.5,
  y: 0.5,
  w: 9,
  h: 0.8,
  fontSize: 40,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

const contents = [
  { title: "1. 课题背景与研究意义", subtitle: "为什么做这个项目？" },
  { title: "2. 国内外研究现状", subtitle: "行业现状与痛点分析" },
  { title: "3. 研究目标与主要内容", subtitle: "项目要实现什么功能？" },
  { title: "4. 系统方案与关键技术", subtitle: "技术架构与核心特性" },
  { title: "5. 系统架构与核心难点", subtitle: "系统设计与解决方案" },
  { title: "6. 预期成果与进度安排", subtitle: "交付成果与开发计划" }
];

let yPos = 1.8;
const itemHeight = 0.9;

contents.forEach((item, index) => {
  // 背景卡片
  slide2.addShape(pres.ShapeType.rect, {
    x: 0.8,
    y: yPos,
    w: 8.4,
    h: itemHeight,
    fill: { color: "transparent" },
    line: { color: colors.primary, width: 2, type: "dash" }
  });

  // 数字圆圈
  slide2.addShape(pres.ShapeType.ellipse, {
    x: 1,
    y: yPos + 0.1,
    w: 0.7,
    h: 0.7,
    fill: { color: colors.primary }
  });

  slide2.addText((index + 1).toString(), {
    x: 1,
    y: yPos + 0.1,
    w: 0.7,
    h: 0.7,
    fontSize: 28,
    bold: true,
    color: colors.white,
    align: "center",
    valign: "middle",
    fontFace: "Arial"
  });

  // 标题
  slide2.addText(item.title, {
    x: 2,
    y: yPos + 0.1,
    w: 7,
    h: 0.4,
    fontSize: 22,
    bold: true,
    color: colors.dark,
    fontFace: "Microsoft YaHei"
  });

  // 副标题
  slide2.addText(item.subtitle, {
    x: 2,
    y: yPos + 0.5,
    w: 7,
    h: 0.3,
    fontSize: 14,
    color: colors.secondary,
    fontFace: "Microsoft YaHei"
  });

  yPos += itemHeight + 0.3;
});

// ========== 第3页：课题背景 ==========
const slide3 = pres.addSlide();
slide3.background = { color: colors.light };

slide3.addText("1. 课题背景", {
  x: 0.5,
  y: 0.4,
  w: 9,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

// 背景介绍
slide3.addText([
  { text: "传统考试模式面临三大核心痛点\n\n", options: { fontSize: 20, bold: true, color: colors.dark } },
  { text: "周期长，工作量大\n", options: { fontSize: 18, color: colors.dark, bullet: true } },
  { text: "人工出题、印刷、监考、阅卷，流程繁琐\n", options: { fontSize: 14, color: colors.dark } },
  { text: " ", options: { fontSize: 14 } },
  { text: "成本高，易出错\n", options: { fontSize: 18, color: colors.dark, bullet: true } },
  { text: "纸张印刷费用昂贵，人工统分容易出错\n", options: { fontSize: 14, color: colors.dark } },
  { text: " ", options: { fontSize: 14 } },
  { text: "效率低，分析难\n", options: { fontSize: 18, color: colors.dark, bullet: true } },
  { text: "无数据分析支持，难以掌握学生学习情况\n", options: { fontSize: 14, color: colors.dark } }
], {
  x: 0.8,
  y: 1.5,
  w: 8.4,
  h: 3.5,
  fontFace: "Microsoft YaHei"
});

// 右侧统计数字
slide3.addShape(pres.ShapeType.rect, {
  x: 1,
  y: 5.3,
  w: 1.8,
  h: 1.6,
  fill: { color: colors.accent },
  line: { color: "transparent" }
});
slide3.addText("50%", {
  x: 1,
  y: 5.3,
  w: 1.8,
  h: 1,
  fontSize: 48,
  bold: true,
  color: colors.white,
  align: "center",
  fontFace: "Arial"
});
slide3.addText("阅卷时间", {
  x: 1,
  y: 6.2,
  w: 1.8,
  h: 0.7,
  fontSize: 16,
  color: colors.white,
  align: "center",
  valign: "middle",
  fontFace: "Microsoft YaHei"
});

slide3.addShape(pres.ShapeType.rect, {
  x: 4.1,
  y: 5.3,
  w: 1.8,
  h: 1.6,
  fill: { color: colors.primary },
  line: { color: "transparent" }
});
slide3.addText("85%", {
  x: 4.1,
  y: 5.3,
  w: 1.8,
  h: 1,
  fontSize: 48,
  bold: true,
  color: colors.white,
  align: "center",
  fontFace: "Arial"
});
slide3.addText("成本降低", {
  x: 4.1,
  y: 6.2,
  w: 1.8,
  h: 0.7,
  fontSize: 16,
  color: colors.white,
  align: "center",
  valign: "middle",
  fontFace: "Microsoft YaHei"
});

slide3.addShape(pres.ShapeType.rect, {
  x: 7.2,
  y: 5.3,
  w: 1.8,
  h: 1.6,
  fill: { color: colors.secondary },
  line: { color: "transparent" }
});
slide3.addText("100%", {
  x: 7.2,
  y: 5.3,
  w: 1.8,
  h: 1,
  fontSize: 48,
  bold: true,
  color: colors.white,
  align: "center",
  fontFace: "Arial"
});
slide3.addText("数据沉淀", {
  x: 7.2,
  y: 6.2,
  w: 1.8,
  h: 0.7,
  fontSize: 16,
  color: colors.white,
  align: "center",
  valign: "middle",
  fontFace: "Microsoft YaHei"
});

// ========== 第4页：研究意义 ==========
const slide4 = pres.addSlide();
slide4.background = { color: colors.light };

slide4.addText("2. 研究意义", {
  x: 0.5,
  y: 0.4,
  w: 9,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

const meanings = [
  {
    icon: "📊",
    title: "教学效率提升",
    desc: "自动化阅卷，解放教师人力；题库电子化，便于检索复用"
  },
  {
    icon: "📈",
    title: "数据驱动教学",
    desc: "成绩分布分析、知识点得分率统计，为教学调整提供数据支撑"
  },
  {
    icon: "🌱",
    title: "绿色环保发展",
    desc: "无纸化考试，大幅降低纸张耗材成本，响应低碳环保理念"
  }
];

let meaningY = 1.8;
meanings.forEach((item, index) => {
  // 图标圆圈
  slide4.addShape(pres.ShapeType.ellipse, {
    x: 1,
    y: meaningY,
    w: 0.9,
    h: 0.9,
    fill: { color: colors.accent }
  });

  slide4.addText(item.icon, {
    x: 1,
    y: meaningY,
    w: 0.9,
    h: 0.9,
    fontSize: 42,
    align: "center",
    valign: "middle"
  });

  // 标题
  slide4.addText(item.title, {
    x: 2.2,
    y: meaningY + 0.05,
    w: 7,
    h: 0.4,
    fontSize: 24,
    bold: true,
    color: colors.primary,
    fontFace: "Microsoft YaHei"
  });

  // 描述
  slide4.addText(item.desc, {
    x: 2.2,
    y: meaningY + 0.5,
    w: 7,
    h: 0.5,
    fontSize: 15,
    color: colors.dark,
    fontFace: "Microsoft YaHei"
  });

  meaningY += 1.5;
});

// ========== 第5页：国内外研究现状 ==========
const slide5 = pres.addSlide();
slide5.background = { color: colors.light };

slide5.addText("3. 国内外研究现状", {
  x: 0.5,
  y: 0.4,
  w: 9,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

// 国内现状
slide5.addText("🏢 国内市场", {
  x: 0.8,
  y: 1.5,
  w: 4,
  h: 0.5,
  fontSize: 20,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

slide5.addText([
  { text: "大型商业平台\n", options: { fontSize: 16, bold: true, color: colors.dark } },
  { text: "超星学习通、雨课堂、问卷星\n", options: { fontSize: 13, bullet: true } },
  { text: "✓ 功能全面、并发能力强\n", options: { fontSize: 12, color: colors.secondary } },
  { text: "✗ 私有化部署困难，费用高昂\n\n", options: { fontSize: 12, color: "C0392B" } },
  { text: "高校自研系统\n", options: { fontSize: 16, bold: true, color: colors.dark } },
  { text: "✗ 技术栈陈旧（JSP、Struts）\n", options: { fontSize: 12, color: "C0392B" } },
  { text: "✗ 界面老旧，移动端适配差\n", options: { fontSize: 12, color: "C0392B" } }
], {
  x: 0.8,
  y: 2.1,
  w: 4,
  h: 3,
  fontFace: "Microsoft YaHei"
});

// 国外现状
slide5.addText("🌍 国外市场", {
  x: 5.2,
  y: 1.5,
  w: 4,
  h: 0.5,
  fontSize: 20,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

slide5.addText([
  { text: "LMS 学习管理系统\n", options: { fontSize: 16, bold: true, color: colors.dark } },
  { text: "Moodle, Canvas, Blackboard\n", options: { fontSize: 13, bullet: true } },
  { text: "✓ 生态完善，插件丰富\n", options: { fontSize: 12, color: colors.secondary } },
  { text: "✗ 系统过于庞大，部署复杂\n", options: { fontSize: 12, color: "C0392B" } },
  { text: "✗ 二次开发门槛高\n", options: { fontSize: 12, color: "C0392B" } }
], {
  x: 5.2,
  y: 2.1,
  w: 4,
  h: 1.5,
  fontFace: "Microsoft YaHei"
});

// 痛点总结
slide5.addShape(pres.ShapeType.rect, {
  x: 0.8,
  y: 5.5,
  w: 8.4,
  h: 1.5,
  fill: { color: "FDEBD0" },
  line: { color: colors.accent, width: 2 }
});

slide5.addText([
  { text: "💡 核心痛点", options: { fontSize: 18, bold: true, color: colors.dark } },
  { text: "\n市场缺乏一款轻量、现代化前后端分离、私有化部署友好的在线考试系统。本项目将填补这一空白。",
    options: { fontSize: 14, color: colors.dark } }
], {
  x: 1.2,
  y: 5.7,
  w: 7.6,
  h: 1.3,
  fontFace: "Microsoft YaHei"
});

// ========== 第6页：研究目标 ==========
const slide6 = pres.addSlide();
slide6.background = { color: colors.light };

slide6.addText("4. 研究目标与系统角色", {
  x: 0.5,
  y: 0.4,
  w: 9,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

// 三类角色
const roles = [
  {
    name: "学生端",
    icon: "👨‍🎓",
    color: "3498DB",
    features: ["在线考试", "实时倒计时", "自动保存", "成绩查询"]
  },
  {
    name: "教师端",
    icon: "👨‍🏫",
    color: "E67E22",
    features: ["题库管理", "试卷组卷", "阅卷评分", "统计分析"]
  },
  {
    name: "管理端",
    icon: "👨‍💼",
    color: "27AE60",
    features: ["用户管理", "课程发布", "系统设置", "权限控制"]
  }
];

let roleX = 0.6;
roles.forEach((role, index) => {
  // 角色卡片
  slide6.addShape(pres.ShapeType.roundRect, {
    x: roleX,
    y: 1.7,
    w: 3,
    h: 4.5,
    fill: { color: "FFFFFF" },
    line: { color: role.color, width: 3 }
  });

  // 图标
  slide6.addText(role.icon, {
    x: roleX + 1,
    y: 2,
    w: 1,
    h: 0.8,
    fontSize: 48,
    align: "center",
    valign: "middle"
  });

  // 角色名
  slide6.addText(role.name, {
    x: roleX,
    y: 3,
    w: 3,
    h: 0.5,
    fontSize: 20,
    bold: true,
    color: role.color,
    align: "center",
    fontFace: "Microsoft YaHei"
  });

  // 功能列表
  let featureY = 3.7;
  role.features.forEach((feature) => {
    slide6.addText(`• ${feature}`, {
      x: roleX + 0.3,
      y: featureY,
      w: 2.4,
      h: 0.35,
      fontSize: 13,
      color: colors.dark,
      fontFace: "Microsoft YaHei"
    });
    featureY += 0.4;
  });

  roleX += 3.1;
});

// 底部总结
slide6.addText("核心目标：高可用性、良好用户体验、清晰的权限控制", {
  x: 0.8,
  y: 6.4,
  w: 8.4,
  h: 0.6,
  fontSize: 16,
  bold: true,
  color: colors.primary,
  align: "center",
  fontFace: "Microsoft YaHei"
});

// ========== 第7页：技术架构 ==========
const slide7 = pres.addSlide();
slide7.background = { color: colors.light };

slide7.addText("5. 技术方案与架构设计", {
  x: 0.5,
  y: 0.4,
  w: 9,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

// 左侧：后端技术栈
slide7.addText("后端技术栈", {
  x: 0.8,
  y: 1.5,
  w: 4,
  h: 0.5,
  fontSize: 20,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

const backendTech = [
  { name: "Spring Boot 3.2.x", desc: "核心框架" },
  { name: "MyBatis-Plus 3.5.x", desc: "持久层框架" },
  { name: "JWT 0.12.x", desc: "无状态认证" },
  { name: "MySQL 8.x", desc: "关系型数据库" },
  { name: "Lombok + Hutool", desc: "开发效率工具" }
];

let backendY = 2.1;
backendTech.forEach((tech, index) => {
  slide7.addShape(pres.ShapeType.rect, {
    x: 0.8,
    y: backendY,
    w: 4,
    h: 0.6,
    fill: { color: index % 2 === 0 ? "EBF5FB" : "FFFFFF" },
    line: { color: colors.secondary, width: 1 }
  });

  slide7.addText([
    { text: tech.name, options: { fontSize: 15, bold: true, color: colors.dark } },
    { text: `  ${tech.desc}`, options: { fontSize: 12, color: colors.dark } }
  ], {
    x: 1,
    y: backendY + 0.15,
    w: 3.6,
    h: 0.35,
    fontFace: "Microsoft YaHei"
  });

  backendY += 0.6;
});

// 右侧：前端技术栈
slide7.addText("前端技术栈", {
  x: 5,
  y: 1.5,
  w: 4,
  h: 0.5,
  fontSize: 20,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

const frontendTech = [
  { name: "Vue 3", desc: "响应式框架" },
  { name: "Element Plus", desc: "UI 组件库" },
  { name: "Pinia", desc: "状态管理" },
  { name: "Axios", desc: "HTTP 客户端" },
  { name: "ECharts", desc: "数据可视化" }
];

let frontendY = 2.1;
frontendTech.forEach((tech, index) => {
  slide7.addShape(pres.ShapeType.rect, {
    x: 5.2,
    y: frontendY,
    w: 4,
    h: 0.6,
    fill: { color: index % 2 === 0 ? "EBF5FB" : "FFFFFF" },
    line: { color: colors.secondary, width: 1 }
  });

  slide7.addText([
    { text: tech.name, options: { fontSize: 15, bold: true, color: colors.dark } },
    { text: `  ${tech.desc}`, options: { fontSize: 12, color: colors.dark } }
  ], {
    x: 5.4,
    y: frontendY + 0.15,
    w: 3.6,
    h: 0.35,
    fontFace: "Microsoft YaHei"
  });

  frontendY += 0.6;
});

// 架构说明
slide7.addShape(pres.ShapeType.rect, {
  x: 0.8,
  y: 5.2,
  w: 8.4,
  h: 1.8,
  fill: { color: colors.primary },
  line: { color: "transparent" }
});

slide7.addText([
  { text: "🏗️ 系统架构", options: { fontSize: 18, bold: true, color: colors.white } },
  { text: "\nB/S 架构 + 前后端分离\n", options: { fontSize: 14, color: colors.white } },
  { text: "• RESTful API 交互，支持后续扩展移动端\n", options: { fontSize: 12, color: colors.white } },
  { text: "• JWT 无状态认证，提升系统性能\n", options: { fontSize: 12, color: colors.white } },
  { text: "• MySQL 数据持久化，Redis 缓存优化（可选）", options: { fontSize: 12, color: colors.white } }
], {
  x: 1.2,
  y: 5.4,
  w: 7.6,
  h: 1.6,
  fontFace: "Microsoft YaHei"
});

// ========== 第8页：核心难点与解决方案 ==========
const slide8 = pres.addSlide();
slide8.background = { color: colors.light };

slide8.addText("6. 核心难点与解决方案", {
  x: 0.5,
  y: 0.4,
  w: 9,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

const problems = [
  {
    title: "考试防作弊机制",
    icon: "🔒",
    color: "C0392B",
    solution: "前端监听 visibilitychange + onblur 事件，记录切屏次数；超过阈值自动交卷"
  },
  {
    title: "答题异常中断处理",
    icon: "💾",
    color: "E67E22",
    solution: "浏览器本地缓存 + 定时异步提交心跳，断网重连后可恢复答题进度"
  },
  {
    title: "随机组卷算法优化",
    icon: "🎲",
    color: "2980B9",
    solution: "按难度系数、知识点权重智能抽题，使用事务保证数据一致性"
  }
];

let problemY = 1.5;
problems.forEach((problem, index) => {
  // 问题卡片
  slide8.addShape(pres.ShapeType.roundRect, {
    x: 0.8,
    y: problemY,
    w: 8.4,
    h: 1.5,
    fill: { color: "FFFFFF" },
    line: { color: problem.color, width: 2 }
  });

  // 图标圆圈
  slide8.addShape(pres.ShapeType.ellipse, {
    x: 1,
    y: problemY + 0.15,
    w: 0.8,
    h: 0.8,
    fill: { color: problem.color }
  });

  slide8.addText(problem.icon, {
    x: 1,
    y: problemY + 0.15,
    w: 0.8,
    h: 0.8,
    fontSize: 36,
    align: "center",
    valign: "middle"
  });

  // 标题
  slide8.addText(problem.title, {
    x: 2,
    y: problemY + 0.2,
    w: 7,
    h: 0.4,
    fontSize: 18,
    bold: true,
    color: problem.color,
    fontFace: "Microsoft YaHei"
  });

  // 解决方案
  slide8.addText([
    { text: "解决方案：", options: { fontSize: 14, bold: true, color: colors.dark } },
    { text: problem.solution, options: { fontSize: 13, color: colors.dark } }
  ], {
    x: 2,
    y: problemY + 0.65,
    w: 6.8,
    h: 0.6,
    fontFace: "Microsoft YaHei"
  });

  problemY += 1.7;
});

// ========== 第9页：预期成果 ==========
const slide9 = pres.addSlide();
slide9.background = { color: colors.light };

slide9.addText("7. 预期交付成果", {
  x: 0.5,
  y: 0.4,
  w: 9,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

const deliverables = [
  {
    number: "01",
    title: "软件系统源码",
    desc: "完整的前后端工程代码，包含 59 个 Java 文件、10 个业务模块",
    color: "3498DB"
  },
  {
    number: "02",
    title: "数据库脚本",
    desc: "MySQL 建表语句、初始化数据 SQL 脚本",
    color: "1ABC9C"
  },
  {
    number: "03",
    title: "设计文档",
    desc: "需求分析、系统设计、E-R 图、接口文档",
    color: "9B59B6"
  },
  {
    number: "04",
    title: "毕业论文",
    desc: "符合学术规范的完整毕业设计论文",
    color: "E74C3C"
  }
];

let deliY = 1.6;
deliverables.forEach((item) => {
  // 背景卡片
  slide9.addShape(pres.ShapeType.rect, {
    x: 0.8,
    y: deliY,
    w: 8.4,
    h: 1.2,
    fill: { color: "FFFFFF" },
    line: { color: item.color, width: 2 }
  });

  // 数字圆圈
  slide9.addShape(pres.ShapeType.ellipse, {
    x: 1.1,
    y: deliY + 0.2,
    w: 0.8,
    h: 0.8,
    fill: { color: item.color }
  });

  slide9.addText(item.number, {
    x: 1.1,
    y: deliY + 0.2,
    w: 0.8,
    h: 0.8,
    fontSize: 28,
    bold: true,
    color: colors.white,
    align: "center",
    valign: "middle",
    fontFace: "Arial"
  });

  // 标题
  slide9.addText(item.title, {
    x: 2.2,
    y: deliY + 0.15,
    w: 6.7,
    h: 0.4,
    fontSize: 18,
    bold: true,
    color: item.color,
    fontFace: "Microsoft YaHei"
  });

  // 描述
  slide9.addText(item.desc, {
    x: 2.2,
    y: deliY + 0.6,
    w: 6.7,
    h: 0.4,
    fontSize: 13,
    color: colors.dark,
    fontFace: "Microsoft YaHei"
  });

  deliY += 1.3;
});

// ========== 第10页：进度安排 ==========
const slide10 = pres.addSlide();
slide10.background = { color: colors.light };

slide10.addText("8. 项目进度安排", {
  x: 0.5,
  y: 0.4,
  w: 9,
  h: 0.8,
  fontSize: 36,
  bold: true,
  color: colors.primary,
  fontFace: "Microsoft YaHei"
});

const schedule = [
  { week: "第 1-2 周", task: "需求分析与文献综述，完成开题报告" },
  { week: "第 3-4 周", task: "UML 用例图设计，E-R 图与数据库设计" },
  { week: "第 5-7 周", task: "后端环境搭建，核心接口开发" },
  { week: "第 8-10 周", task: "前端开发，前后端联调，在线答题模块实现" },
  { week: "第 11-12 周", task: "系统集成测试，Bug 修复，极端场景处理" },
  { week: "第 13-14 周", task: "文档整理与毕业论文撰写" },
  { week: "第 15-16 周", task: "论文定稿与答辩准备" }
];

let scheY = 1.5;
schedule.forEach((item, index) => {
  const isEven = index % 2 === 0;
  slide10.addShape(pres.ShapeType.rect, {
    x: 0.8,
    y: scheY,
    w: 8.4,
    h: 0.7,
    fill: { color: isEven ? "EBF5FB" : "D6EAF8" },
    line: { color: colors.primary, width: 1 }
  });

  // 周次
  slide10.addText(item.week, {
    x: 1.1,
    y: scheY + 0.1,
    w: 1.8,
    h: 0.5,
    fontSize: 14,
    bold: true,
    color: colors.primary,
    fontFace: "Microsoft YaHei"
  });

  // 任务
  slide10.addText(item.task, {
    x: 3.1,
    y: scheY + 0.1,
    w: 5.8,
    h: 0.5,
    fontSize: 14,
    color: colors.dark,
    fontFace: "Microsoft YaHei"
  });

  scheY += 0.7;
});

// ========== 第11页：结束页 ==========
const slide11 = pres.addSlide();
slide11.background = { color: colors.primary };

slide11.addText("感谢各位评委老师的聆听！", {
  x: 1,
  y: 3,
  w: 8,
  h: 1,
  fontSize: 42,
  bold: true,
  color: colors.white,
  align: "center",
  fontFace: "Microsoft YaHei"
});

slide11.addText("请批评指正", {
  x: 1,
  y: 4.2,
  w: 8,
  h: 0.6,
  fontSize: 28,
  color: "B4D4E8",
  align: "center",
  fontFace: "Microsoft YaHei"
});

// 底部装饰线
slide11.addShape(pres.ShapeType.line, {
  x: 1.5,
  y: 5.5,
  w: 7,
  h: 0,
  line: { color: "B4D4E8", width: 3 }
});

slide11.addText("南方职业学院 · 2026届毕业设计", {
  x: 2,
  y: 6,
  w: 6,
  h: 0.5,
  fontSize: 16,
  color: colors.white,
  align: "center",
  fontFace: "Microsoft YaHei"
});

// 保存文件
pres.writeFile({ fileName: "开题答辩.pptx" })
  .then(fileName => {
    console.log(`✅ PPT 已生成: ${fileName}`);
  })
  .catch(err => {
    console.error("❌ 生成失败:", err);
  });
