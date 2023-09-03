package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.data.CompletableInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.VortexSearchClan;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 用户公会信息o
 *
 * @author Xun
 * @date 2023/5/22 21:42 星期一
 */
public record WowsHttpClanTools(HttpClient httpClient, WowsServer server) {

    public Developers developers(String token) {
        return new Developers(new JsonUtils(), httpClient, server, token);
    }

    public Vortex vortex() {
        return new Vortex(new JsonUtils(), httpClient, server);
    }

    public record Developers(JsonUtils utils, HttpClient httpClient, WowsServer server, String token) {

        public CompletableFuture<CompletableInfo<DevelopersSearchUserClan>> userSearchClanDevelopers(long accountId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(userSearchClanDevelopersUri(accountId))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersSearchUserClan.parse(utils, accountId, HttpCodec.response(data)));
                } catch (StatusException | IOException | HttpStatusException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public CompletableFuture<CompletableInfo<DevelopersClanInfo>> clanInfoDevelopers(long clanId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(clanInfoDevelopersUri(clanId))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersClanInfo.parse(utils, clanId, HttpCodec.response(data)));
                } catch (StatusException | IOException | HttpStatusException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public CompletableFuture<CompletableInfo<List<DevelopersSearchClan>>> searchClanDevelopers(String clanTag) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(searchClanDevelopersUri(clanTag))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersSearchClan.parse(utils, HttpCodec.response(data)));
                } catch (StatusException | IOException | HttpStatusException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public URI userSearchClanDevelopersUri(long accountId) {
            return URI.create(server.api() + String.format("/wows/clans/accountinfo/?application_id=%s&account_id=%s&extra=clan", token, accountId));
        }

        public URI clanInfoDevelopersUri(long clanId) {
            return URI.create(server.api() + String.format("/wows/clans/info/?extra=members&application_id=%s&clan_id=%s", token, clanId));
        }

        public URI searchClanDevelopersUri(String clanTag) {
            return URI.create(server.api() + String.format("/wows/clans/list/?application_id=%s&search=%s", token, HttpCodec.encodeURIComponent(clanTag)));
        }
    }

    public record Vortex(JsonUtils utils, HttpClient httpClient, WowsServer server) {

        /**
         * 搜索用户公会信息
         *
         * @param accountId
         * @return
         * @throws HttpStatusException
         * @throws IOException
         * @throws InterruptedException
         * @throws StatusException
         */
        public CompletableFuture<CompletableInfo<VortexSearchClanUser>> userSearchClanVortex(long accountId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(userSearchClanVortexUri(accountId))).thenApplyAsync(data -> {
                try {
                    if (server.isApi()) {
                        return CompletableInfo.ok(VortexSearchClanUser.to(utils.parse(HttpCodec.response(data))));
                    }
                    //处理404的情况
                    if (data.statusCode() == 404) {
                        return CompletableInfo.ok(new VortexSearchClanUser("", VortexSearchClanUser.VortexSearchClanInfo.empty(), "", 0));
                    }
                    return CompletableInfo.ok(VortexSearchClanUser.to(utils.parse(HttpCodec.response(data))));
                } catch (StatusException | HttpStatusException | IOException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        /**
         * 查找公会
         *
         * @param clanTag 公会tag
         * @return
         * @throws HttpStatusException
         * @throws IOException
         * @throws InterruptedException
         */
        public CompletableFuture<CompletableInfo<List<VortexSearchClan>>> searchClanVortex(String clanTag) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(searchClanVortexUri(clanTag))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(VortexSearchClan.parse(utils, HttpCodec.response(data)));
                } catch (HttpStatusException | IOException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public CompletableFuture<CompletableInfo<VortexClanInfo>> clanInfoVortex(long clanId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(clanInfoVortexUri(clanId))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(VortexClanInfo.to(server, clanId, utils.parse(HttpCodec.response(data))));
                } catch (HttpStatusException | IOException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public CompletableFuture<CompletableInfo<List<VortexClanUserInfo>>> clanUserListInfoVortex(long clanId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(clanUserListInfoVortexUri(clanId))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(VortexClanUserInfo.to(server, utils.parse(HttpCodec.response(data))));
                } catch (HttpStatusException | IOException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public URI userSearchClanVortexUri(long accountId) {
            return URI.create(server.vortex() + String.format("/api/accounts/%s/clans/", accountId));
        }

        public URI clanInfoVortexUri(long clanId) {
            return URI.create(server.clans() + String.format("/api/clanbase/%s/claninfo/", clanId));
        }

        public URI clanUserListInfoVortexUri(long clanId) {
            return URI.create(server.clans() + String.format("/api/members/%s/?battle_type=pvp", clanId));
        }

        public URI searchClanVortexUri(String clanTag) {
            return URI.create(server.clans() + String.format("/api/search/autocomplete/?search=%s&type=clans", HttpCodec.encodeURIComponent(clanTag)));
        }
    }

}
