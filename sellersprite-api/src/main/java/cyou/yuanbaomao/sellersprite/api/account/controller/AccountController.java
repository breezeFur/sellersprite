// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.account.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SellerSprite 账户次数", description = "SellerSprite 账户次数分类接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellersprite/account")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "可用次数查询", description = "通过统一 SellerSpriteClient 调用 /v1/visits")
    @GetMapping("/visits")
    public Result<VisitsVo> getVisits() {
        return Result.success(accountService.getVisits());
    }

}
