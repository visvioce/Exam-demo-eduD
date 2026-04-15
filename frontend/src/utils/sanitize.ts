/**
 * HTML 内容净化工具
 * 用于防止 XSS 攻击，净化用户输入的 HTML 内容
 */

/**
 * 允许的 HTML 标签白名单
 */
const ALLOWED_TAGS = [
  'p', 'br', 'span', 'div',
  'strong', 'b', 'em', 'i', 'u', 's',
  'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'ul', 'ol', 'li',
  'table', 'thead', 'tbody', 'tr', 'th', 'td',
  'img', 'a',
  'code', 'pre', 'blockquote'
]

/**
 * 允许的 HTML 属性白名单
 */
const ALLOWED_ATTRIBUTES: Record<string, string[]> = {
  'img': ['src', 'alt', 'title', 'width', 'height'],
  'a': ['href', 'title', 'target'],
  'span': ['class'],
  'div': ['class'],
  'table': ['class', 'border'],
  'td': ['colspan', 'rowspan'],
  'th': ['colspan', 'rowspan']
}

/**
 * 允许的 URL 协议白名单
 */
const ALLOWED_PROTOCOLS = ['http', 'https', 'mailto']

/**
 * 净化 HTML 内容，移除危险的标签和属性
 * @param html 原始 HTML 内容
 * @returns 净化后的安全 HTML
 */
export function sanitizeHtml(html: string): string {
  if (!html) return ''
  
  // 创建临时 DOM 元素进行解析
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html
  
  // 递归清理所有节点
  cleanNode(tempDiv)
  
  return tempDiv.innerHTML
}

/**
 * 递归清理 DOM 节点
 */
function cleanNode(node: Node): void {
  // 从后向前遍历，避免删除节点后索引错乱
  const children = Array.from(node.childNodes)
  
  for (let i = children.length - 1; i >= 0; i--) {
    const child = children[i]
    if (!child) continue
    
    if (child.nodeType === Node.ELEMENT_NODE) {
      const element = child as Element
      const tagName = element.tagName.toLowerCase()
      
      // 移除不在白名单中的标签，但保留其内容
      if (!ALLOWED_TAGS.includes(tagName)) {
        // 特殊处理：完全移除 script、style 等危险标签
        if (['script', 'style', 'iframe', 'object', 'embed', 'form'].includes(tagName)) {
          element.remove()
          continue
        }
        
        // 其他标签保留内容
        const parent = element.parentNode
        while (element.firstChild) {
          parent?.insertBefore(element.firstChild, element)
        }
        element.remove()
        continue
      }
      
      // 清理属性
      cleanAttributes(element, tagName)
      
      // 递归处理子节点
      cleanNode(element)
    } else if (child.nodeType === Node.COMMENT_NODE) {
      // 移除注释节点
      child.remove()
    }
  }
}

/**
 * 清理元素的属性
 */
function cleanAttributes(element: Element, tagName: string): void {
  const allowedAttrs = ALLOWED_ATTRIBUTES[tagName] || []
  const attributes = Array.from(element.attributes)
  
  for (const attr of attributes) {
    const attrName = attr.name.toLowerCase()
    
    // 移除不在白名单中的属性
    if (!allowedAttrs.includes(attrName)) {
      // 特殊处理：移除所有事件处理器属性
      if (attrName.startsWith('on')) {
        element.removeAttribute(attrName)
        continue
      }
      
      // 移除危险的属性
      if (['style', 'id', 'data-', 'srcdoc'].some(dangerous => attrName.includes(dangerous))) {
        element.removeAttribute(attrName)
        continue
      }
      
      // 非白名单属性移除
      if (!allowedAttrs.includes(attrName)) {
        element.removeAttribute(attrName)
      }
    }
    
    // 验证 URL 属性
    if (['src', 'href'].includes(attrName)) {
      const value = attr.value
      if (!isSafeUrl(value)) {
        element.removeAttribute(attrName)
      }
    }
  }
}

/**
 * 检查 URL 是否安全
 */
function isSafeUrl(url: string): boolean {
  if (!url) return true
  
  // 允许相对路径
  if (url.startsWith('/') || url.startsWith('#')) {
    return true
  }
  
  // 检查协议
  const protocol = url.split(':')[0]?.toLowerCase()
  // 阻止 data: 协议（可能用于 XSS）
  if (protocol === 'data') return false
  return protocol ? ALLOWED_PROTOCOLS.includes(protocol) : false
}

/**
 * 转义 HTML 特殊字符（用于纯文本显示）
 */
export function escapeHtml(text: string): string {
  if (!text) return ''
  
  const escapeMap: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }
  
  return text.replace(/[&<>"']/g, char => escapeMap[char] || char)
}

/**
 * 截断文本并添加省略号
 */
export function truncateText(text: string, maxLength: number): string {
  if (!text || text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

/**
 * 净化并截断 HTML 内容
 */
export function sanitizeAndTruncate(html: string, maxLength: number): string {
  const sanitized = sanitizeHtml(html)
  // 移除 HTML 标签获取纯文本长度
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = sanitized
  const textContent = tempDiv.textContent || ''
  
  if (textContent.length <= maxLength) {
    return sanitized
  }
  
  // 如果需要截断，返回纯文本
  return escapeHtml(truncateText(textContent, maxLength))
}

export default {
  sanitizeHtml,
  escapeHtml,
  truncateText,
  sanitizeAndTruncate
}
