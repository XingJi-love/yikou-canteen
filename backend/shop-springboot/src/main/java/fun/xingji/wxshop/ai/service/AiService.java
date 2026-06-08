package fun.xingji.wxshop.ai.service;

import fun.xingji.wxshop.ai.dto.AiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AI 核心服务 — 拼装 System/User Prompt，调用 ChatClient（阿里云百炼），返回 AiResponse。
 * 异常由 callAi() 统一转为 AiServiceException，GlobalExceptionHandler 兜底。
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class); // 日志：记录模型名、调用失败

    /**
     * 大模型名称 — 从 application.yml 读取，支持动态切换（如 deepseek-v3 / qwen-plus）
     */
    @Value("${spring.ai.dashscope.chat.options.model:deepseek-v3}")
    private String modelName;

    /**
     * ChatClient — 由 AiConfig 注册，底层通过 DashScopeChatModel 调用阿里云百炼 API
     */
    private final ChatClient chatClient;

    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. 菜品描述生成
    // ═══════════════════════════════════════════════════════════════

    /**
     * 生成港式茶餐厅菜品描述，15-25字，使用镬气/秘制/招牌等特色词汇。
     */
    public AiResponse generateFoodDescription(String foodName, String category, String price) {
        // System Prompt：设定 AI 角色为「港式茶餐厅美食文案写手」+ 输出规范
        String systemPrompt = """
                你是一位资深美食文案写手，专注于港式茶餐厅风格的菜品描述。
                要求：
                1. 描述字数严格控制在 15-25 字之间，一句话说完，绝不超过25字
                2. 使用生动、诱人的语言，突出菜品的口感、香气和特色
                3. 风格要贴合港式茶餐厅的地道、亲切感
                4. 适当使用一些港式茶餐厅常见表达（如"够镬气""秘制""招牌""即叫即做"等）
                5. 只输出菜品描述文案本身，不要前缀说明、引号、编号等多余内容
                """;

        // User Prompt：拼接菜品具体信息（空值用默认文本兜底）
        String userPrompt = String.format(
                "请为这道港式茶餐厅菜品撰写一段吸引人的描述文案：\n菜品名称：%s\n所属分类：%s\n价格：%s元",
                foodName,
                category != null ? category : "未知分类",  // 分类可为 null，兜底显示"未知分类"
                price != null ? price : "待定"              // 价格可为 null，兜底显示"待定"
        );

        // 调用大模型生成描述，包裹为统一响应体
        String content = callAi(systemPrompt, userPrompt);
        return new AiResponse(content, modelName);
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. 每日经营简报
    // ═══════════════════════════════════════════════════════════════

    /**
     * 生成每日经营简报：核心数据→对比趋势→异常提醒→经营建议。
     */
    public AiResponse generateDailyBrief(String data, String date) {
        // 处理日期显示：前端可选传日期，不传则默认展示"昨天"
        String displayDate = (date != null && !date.isEmpty()) ? date : "昨天";

        // System Prompt：设定 AI 角色为「餐饮经营顾问」+ 分析结构规范
        String systemPrompt = """
                你是一位经验丰富的餐饮行业经营顾问，擅长分析店铺经营数据并给出精准、实用的总结。
                要求：
                1. 先总结当日/近期核心数据（营收、订单量、热卖商品）
                2. 与前一天或上一周期做对比，指出变化趋势（增长/下降用"⬆️⬇️"标识）
                3. 如果发现异常（如某天订单骤降、某商品突然滞销），重点提醒
                4. 最后给出 1-2 条简短的经营建议
                5. 整体风格专业但亲切，适合小餐馆老板阅读
                6. 使用简洁的段落和要点式表达，避免冗长
                不要输出"好的""以下是"等开场白，直接进入分析内容。
                """;

        // User Prompt：将日期+经营数据嵌入模板
        String userPrompt = String.format(
                "请根据以下%s的经营数据，生成一份每日经营简报。\n\n%s",
                displayDate, data
        );

        String content = callAi(systemPrompt, userPrompt);
        return new AiResponse(content, modelName);
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. 智能套餐推荐
    // ═══════════════════════════════════════════════════════════════

    /**
     * 基于高频搭配数据推荐 2-3 个套餐，含名称、理由、定价（比单买便宜10-20%）。
     */
    public AiResponse recommendCombos(String comboData) {
        // System Prompt：设定 AI 角色为「菜单策划专家」+ 套餐命名/定价规范
        String systemPrompt = """
                你是一位餐饮连锁店的菜单策划专家，擅长通过数据分析来设计高转化率的套餐搭配。
                要求：
                1. 从高频菜品搭配数据中，挑出 2-3 个最值得推荐的套餐组合
                2. 每个套餐包含：套餐名称（有吸引力）、组合菜品（2-3个）、推荐理由（一句话）、建议定价
                3. 定价策略：套餐总价应比单独购买便宜 10%-20%，且要考虑满减规则的兼容性
                4. 输出使用清晰的结构化格式，每个套餐之间用空行隔开
                5. 如果数据不足以推荐套餐，诚实说明并给出需要更多数据的建议
                不要输出"好的""以下是"等开场白。
                """;

        // User Prompt：拼接高频搭配数据
        String userPrompt = "请根据以下历史订单数据，推荐最优的菜品套餐搭配方案。\n\n" + comboData;

        String content = callAi(systemPrompt, userPrompt);
        return new AiResponse(content, modelName);
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. 营销活动参谋
    // ═══════════════════════════════════════════════════════════════

    /**
     * 基于店铺数据生成营销活动方案
     * <p>
     * System Prompt 角色：餐饮行业营销策划师，擅长低成本、高回报方案。
     * <p>
     * 每个活动方案包含 4 个要素：
     * <ol>
     *   <li><b>活动主题</b>：吸引眼球的标题</li>
     *   <li><b>适用条件</b>：什么情况下启动该活动</li>
     *   <li><b>具体方案</b>：怎么执行（步骤清晰、可落地）</li>
     *   <li><b>预期效果</b>：达到什么目标（提升客单 / 清库存 / 拉新等）</li>
     * </ol>
     * <p>
     * 特殊约束：
     * <ul>
     *   <li>考虑港式茶餐厅特点：客单价偏低、出餐快、时段明显（早/午/晚市）</li>
     *   <li>满减优化建议应为增量优化，不推翻原有规则</li>
     * </ul>
     * <p>
     * User Prompt 包含 AiDataService.getMarketingData() 聚合的店铺信息、
     * 满减规则、热销排行和营收概览。
     *
     * @param marketingData 店铺营销数据（由 AiDataService 生成）
     * @return AiResponse，content 为 Markdown 格式的营销活动方案
     */
    public AiResponse generateMarketingAdvice(String marketingData) {
        // System Prompt：设定 AI 角色为「营销策划师」+ 活动方案四要素 + 茶餐厅特性
        String systemPrompt = """
                你是一位餐饮行业的营销策划师，擅长设计低成本、高回报的促销活动方案。
                要求：
                1. 结合店铺当前的热销商品、营收数据和满减规则，给出 2-3 个具体的营销活动建议
                2. 每个活动包含：活动主题（吸引眼球）、适用条件（什么情况下启动）、具体方案（怎么做）、预期效果（达到什么目的）
                3. 活动方案要实操性强，考虑港式茶餐厅的特点（客单价偏低、出餐快、时段明显）
                4. 可以提出满减优化建议，但不要推翻原有规则，而是增量优化
                5. 输出使用清晰的结构化格式
                不要输出"好的""以下是"等开场白。
                """;

        // User Prompt：拼接店铺营销全景数据
        String userPrompt = "请根据以下店铺经营数据，设计营销活动方案。\n\n" + marketingData;

        String content = callAi(systemPrompt, userPrompt);
        return new AiResponse(content, modelName);
    }

    // ═══════════════════════════════════════════════════════════════
    // 底层 LLM 调用（同步）
    // ═══════════════════════════════════════════════════════════════

    /**
     * 调用大模型生成文本
     * <p>
     * 使用 Spring AI ChatClient 的 Fluent API：
     * <pre>
     * chatClient.prompt()        // 创建 Prompt 构建器
     *     .system(systemPrompt)  // 设置 System Prompt（角色设定）
     *     .user(userPrompt)      // 设置 User Prompt（具体问题/数据）
     *     .call()                // 同步调用 LLM
     *     .content()             // 提取 AI 回复的文本内容
     * </pre>
     * <p>
     * 异常处理策略：
     * <ul>
     *   <li>网络超时、API Key 无效、模型不存在等异常统一捕获</li>
     *   <li>转换自定义 AiServiceException，避免将底层异常细节暴露给前端</li>
     *   <li>记录完整错误日志（含堆栈），便于排查问题</li>
     * </ul>
     *
     * @param systemPrompt System Prompt（角色设定 + 输出规范）
     * @param userPrompt   User Prompt（具体数据上下文）
     * @return AI 返回的原始文本内容
     * @throws AiServiceException 当 LLM 调用失败时
     */
    private String callAi(String systemPrompt, String userPrompt) {
        try {
            log.info("调用 AI 模型: {}", modelName); // 记录调用的模型名，方便排查
            // Fluent API 链式调用：
            //   .prompt()          → 创建 Prompt 构建器
            //   .system(system)    → 设置 System Prompt（角色 + 规范）
            //   .user(user)        → 设置 User Prompt（数据 + 问题）
            //   .call()            → 同步调用大模型（阻塞等待）
            //   .content()         → 提取 AI 回复的文本内容（去除元数据）
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            // 统一捕获：网络超时、API Key 无效、模型不存在、额度不足等
            log.error("AI 调用失败", e); // 记录完整堆栈，方便排查
            throw new AiServiceException("AI服务暂不可用，请稍后重试"); // 对前端隐藏底层异常细节
        }
    }

    /**
     * AI 服务异常 — 非受检异常，由 GlobalExceptionHandler 统一返回 500。
     */
    public static class AiServiceException extends RuntimeException {
        public AiServiceException(String message) {
            super(message);
        }
    }
}
