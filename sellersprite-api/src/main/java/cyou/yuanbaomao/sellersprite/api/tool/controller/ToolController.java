// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.tool.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;
import cyou.yuanbaomao.sellersprite.api.tool.model.vo.OcrVo;
import cyou.yuanbaomao.sellersprite.api.tool.service.ToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SellerSprite 数据工具", description = "SellerSprite 数据工具分类接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellersprite/tools")
public class ToolController {

    private final ToolService toolService;

    @Operation(summary = "图片文字识别", description = "通过统一 SellerSpriteClient 调用 /v1/ocr")
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<OcrVo> recognizeImageText(@Valid @ModelAttribute OcrRequest request) {
        return Result.success(toolService.recognizeImageText(request));
    }

}
